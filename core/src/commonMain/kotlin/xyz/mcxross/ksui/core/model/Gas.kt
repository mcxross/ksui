/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.core.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.core.crypto.secureRandomBytes
import xyz.mcxross.ksui.core.exception.SuiException
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.core.ptb.SUI_COIN_TYPE
import xyz.mcxross.ksui.core.ptb.TransactionKind
import xyz.mcxross.ksui.core.util.MAX_GAS_PAYMENT_OBJECTS
import xyz.mcxross.ksui.core.util.bcsDecode
import xyz.mcxross.ksui.core.util.bcsEncode

@Serializable
data class GasData(
  val payment: List<ObjectReference>,
  val owner: AccountAddress,
  val price: ULong,
  val budget: ULong,
)

data class GasConfig(
  val source: FundingSource = FundingSource.Auto,
  val budget: ULong? = null,
  val price: ULong? = null,
  val payment: List<ObjectReference>? = null,
  val maxGasCoinSpend: ULong? = null,
  val smashCoins: Boolean = true,
) {
  constructor(
    source: FundingSource,
    budget: ULong?,
    price: ULong?,
    payment: List<ObjectReference>?,
  ) : this(source, budget, price, payment, null, true)
}

data class GasContext(
  val epoch: ULong,
  val referenceGasPrice: ULong,
  val chain: Digest,
  val holdings: Holdings,
)

data class CoinObject(val reference: ObjectReference, val balance: ULong)

data class GasCostSummary(
  val computationCost: ULong,
  val storageCost: ULong,
  val storageRebate: ULong,
)

interface GasEnvironment {
  @Throws(Exception::class) suspend fun context(owner: AccountAddress): GasContext

  @Throws(Exception::class) suspend fun coins(owner: AccountAddress): List<CoinObject>

  @Throws(Exception::class) suspend fun simulate(transaction: TransactionData): GasCostSummary
}

private const val MAX_GAS_BUDGET = 50_000_000_000UL

private const val GAS_SAFE_OVERHEAD = 1_000UL

private val ACCUMULATOR_ROOT = AccountAddress.fromString("0xacc")

private val SUI_BALANCE_KEY =
  TypeTag.Struct(
    StructTag(
      AccountAddress.fromString("0x2"),
      "accumulator",
      "Key",
      listOf(
        TypeTag.Struct(
          StructTag(AccountAddress.fromString("0x2"), "balance", "Balance", listOf(SUI_COIN_TYPE)),
        ),
      ),
    ),
  )

private val COIN_RESERVATION_MAGIC = ByteArray(20) { 0xac.toByte() }

internal suspend fun composeWithGas(
  sender: AccountAddress,
  pt: ProgrammableTransaction,
  gas: GasConfig,
  environment: GasEnvironment,
): TransactionData {
  val context = environment.context(sender)
  val price = gas.price ?: context.referenceGasPrice

  var cachedCoins: List<CoinObject>? = null
  suspend fun loadCoinsOnce(): List<CoinObject> {
    cachedCoins?.let {
      return it
    }
    return environment.coins(sender).also { cachedCoins = it }
  }

  val budget =
    gas.budget
      ?: estimateBudget(
        sender = sender,
        pt = pt,
        price = price,
        gas = gas,
        context = context,
        environment = environment,
        coinsProvider = ::loadCoinsOnce,
      )
  val (payment, usesReservationOnly) =
    if (gas.payment != null) {
      gas.payment to false
    } else {
      selectPayment(sender, pt, gas, budget, context, ::loadCoinsOnce)
    }

  val needsReplayExpiration = payment.isEmpty() || usesReservationOnly
  return TransactionData.V1(
    kind = TransactionKind.ProgrammableTransaction(pt),
    sender = sender,
    gasData = GasData(payment, sender, price, budget),
    expiration = if (needsReplayExpiration) context.validDuring() else TransactionExpiration.None,
  )
}

private suspend fun estimateBudget(
  sender: AccountAddress,
  pt: ProgrammableTransaction,
  price: ULong,
  gas: GasConfig,
  context: GasContext,
  environment: GasEnvironment,
  coinsProvider: suspend () -> List<CoinObject>,
): ULong {
  val usesGasCoin = pt.commands.any { it.usesGasCoin() }
  val simPayment =
    when {
      gas.payment != null -> gas.payment
      usesGasCoin && context.holdings.addressBalance == 0UL ->
        availableCoins(pt, coinsProvider()).take(MAX_GAS_PAYMENT_OBJECTS).map { it.reference }
      else -> emptyList()
    }
  val cost =
    environment.simulate(
      TransactionData.V1(
        kind = TransactionKind.ProgrammableTransaction(pt),
        sender = sender,
        gasData = GasData(simPayment, sender, price, MAX_GAS_BUDGET),
        expiration =
          if (simPayment.isEmpty()) context.validDuring() else TransactionExpiration.None,
      ),
    )
  val overhead = checkedMul(GAS_SAFE_OVERHEAD, price)
  val computation = checkedAdd(cost.computationCost, overhead)
  return if (cost.storageCost > cost.storageRebate) {
    checkedAdd(computation, cost.storageCost - cost.storageRebate)
  } else {
    computation
  }
}

private suspend fun selectPayment(
  sender: AccountAddress,
  pt: ProgrammableTransaction,
  gas: GasConfig,
  budget: ULong,
  context: GasContext,
  coinsProvider: suspend () -> List<CoinObject>,
): Pair<List<ObjectReference>, Boolean> {
  var withdrawn = 0UL
  for (input in pt.inputs) {
    val arg = (input as? CallArg.FundsWithdrawal)?.arg ?: continue
    if (arg.coinType == SUI_COIN_TYPE && arg.source == FundsWithdrawalSource.Sender) {
      withdrawn = checkedAdd(withdrawn, arg.amount)
    }
  }
  val addressBalance =
    context.holdings.addressBalance.let { if (it > withdrawn) it - withdrawn else 0UL }
  val usesGasCoin = pt.commands.any { it.usesGasCoin() }

  val boundedSpend =
    when {
      !usesGasCoin -> 0UL
      gas.maxGasCoinSpend != null -> gas.maxGasCoinSpend
      !gas.smashCoins -> inferPureSplitGasCoinSpend(pt)
      else -> null
    }
  val requiredTotal = boundedSpend?.let { checkedAdd(budget, it) }
  val minimumNeeded = requiredTotal ?: budget

  if (gas.source == FundingSource.AddressBalance) {
    if (addressBalance < minimumNeeded) {
      throw SuiException("Address balance $addressBalance cannot cover required $minimumNeeded")
    }
    return if (!usesGasCoin) {
      emptyList<ObjectReference>() to false
    } else {
      val reserveAmount = requiredTotal ?: addressBalance
      listOf(coinReservation(sender, reserveAmount, context.epoch, context.chain)) to true
    }
  }

  if (gas.source == FundingSource.Auto && !usesGasCoin && addressBalance >= budget) {
    return emptyList<ObjectReference>() to false
  }

  val coins = availableCoins(pt, coinsProvider())
  val reserveSlotForAccumulator =
    gas.source == FundingSource.Auto && addressBalance > 0UL && requiredTotal == null
  val maxCoinObjects =
    if (reserveSlotForAccumulator) MAX_GAS_PAYMENT_OBJECTS - 1 else MAX_GAS_PAYMENT_OBJECTS

  val payment = mutableListOf<ObjectReference>()
  var total = 0UL
  for (coin in coins) {
    if ((requiredTotal != null && total >= requiredTotal) || payment.size == maxCoinObjects) break
    if (requiredTotal != null && payment.size == MAX_GAS_PAYMENT_OBJECTS - 1) {
      val withCoin = checkedAdd(total, coin.balance)
      if (withCoin < requiredTotal && gas.source == FundingSource.Auto && addressBalance > 0UL) {
        break
      }
    }
    payment += coin.reference
    total = checkedAdd(total, coin.balance)
  }

  var addedReservation = false
  if (gas.source == FundingSource.Auto && addressBalance > 0UL) {
    val reserveAmount =
      when {
        requiredTotal == null -> addressBalance
        total < requiredTotal -> minOf(addressBalance, requiredTotal - total)
        else -> 0UL
      }
    if (reserveAmount > 0UL) {
      // Placing the synthetic coin reservation at index 0 smashes real coins and settles change to
      // the sender's address balance per SIP-58 gas-smashing rules.
      payment.add(0, coinReservation(sender, reserveAmount, context.epoch, context.chain))
      total = checkedAdd(total, reserveAmount)
      addedReservation = true
    }
  }

  if (total < minimumNeeded) {
    val coinsTotal = coins.fold(0UL) { acc, c -> saturatingAdd(acc, c.balance) }
    throw SuiException(
      "Insufficient SUI for gas requirement $minimumNeeded: coin objects hold $coinsTotal, " +
        "address balance holds $addressBalance",
    )
  }
  return payment to (addedReservation && payment.size == 1)
}

private fun availableCoins(
  pt: ProgrammableTransaction,
  coins: List<CoinObject>,
): List<CoinObject> {
  val inputObjects =
    pt.inputs
      .mapNotNull { input ->
        when (val arg = (input as? CallArg.Object)?.arg) {
          is ObjectArg.ImmOrOwnedObject -> arg.objectRef.reference.accountAddress
          is ObjectArg.Receiving -> arg.objectRef.reference.accountAddress
          is ObjectArg.SharedObject -> arg.id.hash
          null -> null
        }
      }
      .toSet()
  return coins
    .filter { it.reference.reference.accountAddress !in inputObjects }
    .sortedByDescending { it.balance }
}

private fun inferPureSplitGasCoinSpend(pt: ProgrammableTransaction): ULong? {
  var spend = 0UL
  for (cmd in pt.commands) {
    when (cmd) {
      is Command.SplitCoins -> {
        if (cmd.into.any { it == Argument.GasCoin }) return null
        if (cmd.coin == Argument.GasCoin) {
          for (amtArg in cmd.into) {
            val idx = (amtArg as? Argument.Input)?.index?.toInt() ?: return null
            val pure = pt.inputs.getOrNull(idx) as? CallArg.Pure ?: return null
            if (pure.data.size != 8) return null
            spend = checkedAdd(spend, bcsDecode<ULong>(pure.data))
          }
        }
      }
      else -> if (cmd.usesGasCoin()) return null
    }
  }
  return spend
}

private fun Command.usesGasCoin(): Boolean =
  when (this) {
    is Command.MoveCall -> Argument.GasCoin in moveCall.arguments
    is Command.TransferObjects -> address == Argument.GasCoin || Argument.GasCoin in objects
    is Command.SplitCoins -> coin == Argument.GasCoin || Argument.GasCoin in into
    is Command.MergeCoins -> coin == Argument.GasCoin || Argument.GasCoin in coins
    is Command.MakeMoveVec -> Argument.GasCoin in values
    is Command.Upgrade -> upgradeTicket == Argument.GasCoin
    is Command.Publish -> false
  }

private fun GasContext.validDuring(): TransactionExpiration {
  val bytes = secureRandomBytes(4)
  val nonce =
    ((bytes[0].toUInt() and 0xffu) shl 24) or
      ((bytes[1].toUInt() and 0xffu) shl 16) or
      ((bytes[2].toUInt() and 0xffu) shl 8) or
      (bytes[3].toUInt() and 0xffu)
  return TransactionExpiration.ValidDuring(
    minEpoch = epoch,
    maxEpoch = checkedAdd(epoch, 1UL),
    minTimestamp = null,
    maxTimestamp = null,
    chain = chain,
    nonce = nonce,
  )
}

// Synthetic SIP-58 coin reservation ObjectRef: ID is the accumulator dynamic-field hash XORed with
// the chain digest, and the 32-byte digest packs (amount: u64, epoch: u32, 20-byte 0xac magic).
private fun coinReservation(
  owner: AccountAddress,
  amount: ULong,
  epoch: ULong,
  chain: Digest,
): ObjectReference {
  require(epoch <= UInt.MAX_VALUE.toULong()) { "Epoch exceeds u32 coin reservation limit: $epoch" }
  val fieldId =
    hash(
      Hash.BLAKE2B256,
      byteArrayOf(0xf0.toByte()) +
        ACCUMULATOR_ROOT.data +
        bcsEncode(owner.data.size.toLong()) +
        owner.data +
        bcsEncode<TypeTag>(SUI_BALANCE_KEY),
    )
  val id =
    ByteArray(AccountAddress.LENGTH) { (fieldId[it].toInt() xor chain.data[it].toInt()).toByte() }
  val digest = bcsEncode(amount) + bcsEncode(epoch.toUInt()) + COIN_RESERVATION_MAGIC
  return ObjectReference(Reference(AccountAddress.from(id)), 0, ObjectDigest(Digest(digest)))
}

fun selectTopGasCoins(
  existing: MutableList<CoinObject>,
  incoming: Iterable<CoinObject>,
  limit: Int = MAX_GAS_PAYMENT_OBJECTS,
) {
  for (coin in incoming) {
    if (existing.size < limit) {
      existing += coin
    } else {
      val minIdx = existing.indices.minBy { existing[it].balance }
      if (coin.balance > existing[minIdx].balance) {
        existing[minIdx] = coin
      }
    }
  }
}

private fun checkedAdd(a: ULong, b: ULong): ULong {
  val sum = a + b
  if (sum < a) throw SuiException("Unsigned 64-bit overflow in gas calculation: $a + $b")
  return sum
}

private fun checkedMul(a: ULong, b: ULong): ULong {
  if (a == 0UL || b == 0UL) return 0UL
  val product = a * b
  if (product / a != b) throw SuiException("Unsigned 64-bit overflow in gas calculation: $a * $b")
  return product
}

private fun saturatingAdd(a: ULong, b: ULong): ULong {
  val sum = a + b
  return if (sum < a) ULong.MAX_VALUE else sum
}
