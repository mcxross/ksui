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

import kotlin.random.Random
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.core.exception.SuiException
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.core.ptb.SUI_COIN_TYPE
import xyz.mcxross.ksui.core.ptb.TransactionKind
import xyz.mcxross.ksui.core.util.MAX_GAS_PAYMENT_OBJECTS
import xyz.mcxross.ksui.core.util.bcsEncode

@Serializable
data class GasData(
  val payment: List<ObjectReference>,
  val owner: AccountAddress,
  val price: ULong,
  val budget: ULong,
)

/**
 * How a transaction pays for gas. [FundingSource.Auto] pays from the sender's address balance
 * unless the transaction uses the gas coin or the balance can't cover the budget. A null [budget]
 * is estimated by simulation, a null [price] is the reference gas price, and a non-null [payment]
 * is used as is.
 */
data class GasConfig(
  val source: FundingSource = FundingSource.Auto,
  val budget: ULong? = null,
  val price: ULong? = null,
  val payment: List<ObjectReference>? = null,
)

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

/** The network reads gas resolution needs, implemented by each client over its transport. */
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
          StructTag(AccountAddress.fromString("0x2"), "balance", "Balance", listOf(SUI_COIN_TYPE))
        )
      ),
    )
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
  val budget = gas.budget ?: estimateBudget(sender, pt, price, context, environment)
  val payment = gas.payment ?: selectPayment(sender, pt, gas.source, budget, context, environment)
  return TransactionData.V1(
    kind = TransactionKind.ProgrammableTransaction(pt),
    sender = sender,
    gasData = GasData(payment, sender, price, budget),
    expiration = if (payment.isEmpty()) context.validDuring() else TransactionExpiration.None,
  )
}

private suspend fun estimateBudget(
  sender: AccountAddress,
  pt: ProgrammableTransaction,
  price: ULong,
  context: GasContext,
  environment: GasEnvironment,
): ULong {
  val cost =
    environment.simulate(
      TransactionData.V1(
        kind = TransactionKind.ProgrammableTransaction(pt),
        sender = sender,
        gasData = GasData(emptyList(), sender, price, MAX_GAS_BUDGET),
        expiration = context.validDuring(),
      )
    )
  val computation = cost.computationCost + GAS_SAFE_OVERHEAD * price
  return if (cost.storageCost > cost.storageRebate) {
    computation + cost.storageCost - cost.storageRebate
  } else {
    computation
  }
}

private suspend fun selectPayment(
  sender: AccountAddress,
  pt: ProgrammableTransaction,
  source: FundingSource,
  budget: ULong,
  context: GasContext,
  environment: GasEnvironment,
): List<ObjectReference> {
  val withdrawn =
    pt.inputs.sumOf { input ->
      val arg = (input as? CallArg.FundsWithdrawal)?.arg
      if (arg?.coinType == SUI_COIN_TYPE) arg.amount else 0UL
    }
  val addressBalance =
    context.holdings.addressBalance.let { if (it > withdrawn) it - withdrawn else 0UL }
  val usesGasCoin = pt.commands.any { it.usesGasCoin() }

  if (
    source == FundingSource.AddressBalance ||
      (source == FundingSource.Auto && !usesGasCoin && addressBalance >= budget)
  ) {
    if (addressBalance < budget) {
      throw SuiException("Address balance $addressBalance cannot cover gas budget $budget")
    }
    return emptyList()
  }

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
  val coins =
    environment
      .coins(sender)
      .filter { it.reference.reference.accountAddress !in inputObjects }
      .sortedByDescending { it.balance }

  // Gas payments are smashed into the gas coin, so a transaction that uses it gets every coin and,
  // through a reservation, the address balance too. Otherwise take only what covers the budget.
  val payment = mutableListOf<ObjectReference>()
  var total = 0UL
  for (coin in coins) {
    if ((!usesGasCoin && total >= budget) || payment.size == MAX_GAS_PAYMENT_OBJECTS - 1) break
    payment += coin.reference
    total += coin.balance
  }
  if (source == FundingSource.Auto && addressBalance > 0UL && (usesGasCoin || total < budget)) {
    payment.add(0, coinReservation(sender, addressBalance, context.epoch, context.chain))
    total += addressBalance
  }
  if (total < budget) {
    throw SuiException(
      "Insufficient SUI for gas budget $budget: coin objects hold ${coins.sumOf { it.balance }}, " +
        "address balance holds $addressBalance"
    )
  }
  return payment
}

private fun Command.usesGasCoin(): Boolean {
  val arguments =
    when (this) {
      is Command.MoveCall -> moveCall.arguments
      is Command.TransferObjects -> objects + address
      is Command.SplitCoins -> into + coin
      is Command.MergeCoins -> coins + coin
      is Command.MakeMoveVec -> values
      is Command.Upgrade -> listOf(upgradeTicket)
      else -> emptyList()
    }
  return Argument.GasCoin in arguments
}

private fun GasContext.validDuring(): TransactionExpiration =
  TransactionExpiration.ValidDuring(
    minEpoch = epoch,
    maxEpoch = epoch + 1UL,
    minTimestamp = null,
    maxTimestamp = null,
    chain = chain,
    nonce = Random.nextInt().toUInt(),
  )

// A reservation against the owner's SUI address balance, shaped as a gas coin reference: the id is
// the owner's accumulator field id masked with the chain id, and the digest carries the amount,
// the epoch and a magic suffix.
private fun coinReservation(
  owner: AccountAddress,
  amount: ULong,
  epoch: ULong,
  chain: Digest,
): ObjectReference {
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
  val digest = bcsEncode(amount.toLong()) + bcsEncode(epoch.toInt()) + COIN_RESERVATION_MAGIC
  return ObjectReference(Reference(AccountAddress.from(id)), 0, ObjectDigest(Digest(digest)))
}
