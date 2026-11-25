/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.core.ptb

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.extension.asIdParts
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.FundsWithdrawalArg
import xyz.mcxross.ksui.core.model.FundsWithdrawalSource
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectId
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.StructTag
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.util.MAX_COMMANDS_IN_PTB

@Serializable(with = xyz.mcxross.ksui.core.serializer.ProgrammableTransactionSerializer::class)
data class ProgrammableTransaction
@Throws(IllegalArgumentException::class)
constructor(
  val inputs: List<CallArg>,
  val commands: List<Command>,
) : TransactionKind() {
  init {
    require(commands.size <= MAX_COMMANDS_IN_PTB) { "Maximum number of commands is 1024" }
    require(inputs.none { it is CallArg.ObjectStr }) {
      "ProgrammableTransaction contains unresolved ObjectStr inputs. Call build(sui) first."
    }
  }

  fun toBcs(): ByteArray = Bcs.encodeToByteArray(this)

  fun toTransactionKindBcs(): ByteArray =
    Bcs.encodeToByteArray<TransactionKind>(TransactionKind.ProgrammableTransaction(this))
}

class ProgrammableTransactionBuilder {

  private val inputValues: MutableList<CallArg> = mutableListOf()
  private val inputIndexByArg: MutableMap<BuilderArg, UShort> = mutableMapOf()
  private val objectIndexByNormalizedId: MutableMap<String, UShort> = mutableMapOf()
  private val commands: MutableList<Command> = mutableListOf()

  val list: List<Command>
    get() = commands.toList()

  private fun addInput(arg: BuilderArg, value: CallArg): Argument {
    inputIndexByArg[arg]?.let { return Argument.Input(it) }
    require(inputValues.size <= UShort.MAX_VALUE.toInt()) {
      "Maximum number of PTB inputs is ${UShort.MAX_VALUE}"
    }
    val nextIndex = inputValues.size.toUShort()
    inputValues += value
    inputIndexByArg[arg] = nextIndex
    return Argument.Input(nextIndex)
  }

  private fun appendCommand(command: Command): UShort {
    require(commands.size < MAX_COMMANDS_IN_PTB) {
      "Maximum number of commands is $MAX_COMMANDS_IN_PTB"
    }
    val index = commands.size.toUShort()
    commands += command
    return index
  }

  fun input(bytes: ByteArray, forceSeparate: Boolean = false): Argument {
    val copy = bytes.copyOf()
    val arg =
      if (forceSeparate) {
        BuilderArg.ForcedNonUniquePure(inputValues.size)
      } else {
        BuilderArg.Pure(copy)
      }
    return addInput(arg, CallArg.Pure(data = copy))
  }

  @Throws(IllegalArgumentException::class)
  fun address(str: String): Argument = input(AccountAddress.fromString(str).data)

  fun address(account: Account): Argument = address(account.address)

  fun address(address: AccountAddress): Argument = input(address.data)

  inline fun <reified T> pure(value: T): Argument = input(Bcs.encodeToByteArray(value))

  fun pure(bytes: ByteArray): Argument = input(bytes)

  inline fun <reified T> input(value: T): Argument {
    if (value is ObjectArg) {
      return `object`(value)
    }
    return input(Bcs.encodeToByteArray(value))
  }

  fun system(): Argument = `object`("0x5")

  fun clock(): Argument = `object`("0x6")

  fun random(): Argument = `object`("0x8")

  fun denyList(): Argument = `object`("0x403")

  @Throws(IllegalArgumentException::class)
  fun `object`(id: String): Argument {
    val normalizedId = normalizeSuiObjectId(id)
    objectIndexByNormalizedId[normalizedId]?.let { return Argument.Input(it) }
    val added =
      addInput(BuilderArg.ForcedNonUniqueObject(inputValues.size), CallArg.ObjectStr(normalizedId))
    objectIndexByNormalizedId[normalizedId] = (added as Argument.Input).index
    return added
  }

  fun `object`(objectArg: ObjectArg): Argument =
    addInput(BuilderArg.ForcedNonUniqueObject(inputValues.size), CallArg.Object(objectArg))

  @Throws(IllegalArgumentException::class)
  fun withdrawal(
    amount: ULong,
    coinType: TypeTag = SUI_COIN_TYPE,
    source: FundsWithdrawalSource = FundsWithdrawalSource.Sender,
  ): Argument {
    require(amount > 0uL) { "Withdrawal amount must be positive" }
    return addInput(
      BuilderArg.ForcedNonUniqueWithdrawal(inputValues.size),
      CallArg.FundsWithdrawal(FundsWithdrawalArg(amount, coinType, source)),
    )
  }

  @Throws(IllegalArgumentException::class)
  fun withdrawCoin(
    amount: ULong,
    coinType: TypeTag = SUI_COIN_TYPE,
    source: FundsWithdrawalSource = FundsWithdrawalSource.Sender,
  ): Argument.Result =
    moveCall(
      "0x2::coin::redeem_funds",
      listOf(coinType),
      listOf(withdrawal(amount, coinType, source)),
    )

  @Throws(IllegalArgumentException::class)
  fun withdrawBalance(
    amount: ULong,
    coinType: TypeTag = SUI_COIN_TYPE,
    source: FundsWithdrawalSource = FundsWithdrawalSource.Sender,
  ): Argument.Result =
    moveCall(
      "0x2::balance::redeem_funds",
      listOf(coinType),
      listOf(withdrawal(amount, coinType, source)),
    )

  fun sendCoinToBalance(
    coin: Argument,
    recipient: AccountAddress,
    coinType: TypeTag = SUI_COIN_TYPE,
  ): Argument.Result =
    moveCall("0x2::coin::send_funds", listOf(coinType), listOf(coin, address(recipient)))

  fun sendBalance(
    balance: Argument,
    recipient: AccountAddress,
    coinType: TypeTag = SUI_COIN_TYPE,
  ): Argument.Result =
    moveCall("0x2::balance::send_funds", listOf(coinType), listOf(balance, address(recipient)))

  fun sendCoinToAddressBalance(
    coin: Argument,
    recipient: AccountAddress,
    coinType: TypeTag = SUI_COIN_TYPE,
  ): Argument.Result = sendCoinToBalance(coin, recipient, coinType)

  fun sendBalanceToAddressBalance(
    balance: Argument,
    recipient: AccountAddress,
    coinType: TypeTag = SUI_COIN_TYPE,
  ): Argument.Result = sendBalance(balance, recipient, coinType)

  @Throws(IllegalArgumentException::class)
  fun transferBalance(
    amount: ULong,
    recipient: AccountAddress,
    coinType: TypeTag = SUI_COIN_TYPE,
  ): Argument.Result = sendBalance(withdrawBalance(amount, coinType), recipient, coinType)

  @Throws(IllegalArgumentException::class)
  fun moveCall(
    target: String,
    typeArgs: List<TypeTag> = emptyList(),
    args: List<Argument> = emptyList(),
  ): Argument.Result {
    val parts = target.asIdParts()
    val moveCall =
      ProgrammableMoveCall(
        ObjectId(AccountAddress.fromString(parts.first)),
        parts.second,
        parts.third,
        typeArgs,
        args,
      )
    return Argument.Result(appendCommand(Command.MoveCall(moveCall)))
  }

  fun transferObjects(objects: List<Argument>, address: Argument): Argument.Result =
    Argument.Result(appendCommand(Command.TransferObjects(objects, address)))

  @Throws(IllegalArgumentException::class)
  fun splitCoins(coin: Argument, into: List<Argument>): List<Argument.NestedResult> {
    require(into.isNotEmpty()) { "The 'into' list of amounts cannot be empty." }
    require(into.size <= UShort.MAX_VALUE.toInt()) { "Too many split amounts" }
    val commandIndex = appendCommand(Command.SplitCoins(coin, into))
    return into.indices.map { i -> Argument.NestedResult(commandIndex, i.toUShort()) }
  }

  fun mergeCoins(coin: Argument, coins: List<Argument>): Argument.Result =
    Argument.Result(appendCommand(Command.MergeCoins(coin, coins)))

  fun publish(bytes: List<List<Byte>>, dependencies: List<ObjectId>): Argument.Result =
    Argument.Result(appendCommand(Command.Publish(bytes, dependencies)))

  fun makeMoveVec(typeTag: TypeTag?, values: List<Argument>): Argument.Result =
    Argument.Result(appendCommand(Command.MakeMoveVec(typeTag, values)))

  fun upgrade(
    modules: List<List<Byte>>,
    dependencies: List<ObjectId>,
    packageId: ObjectId,
    upgradeTicket: Argument,
  ): Argument.Result =
    Argument.Result(appendCommand(Command.Upgrade(modules, dependencies, packageId, upgradeTicket)))

  @Throws(IllegalArgumentException::class)
  fun build(): ProgrammableTransaction =
    ProgrammableTransaction(inputValues.toList(), commands.toList())

  @Throws(
    IllegalArgumentException::class,
    IllegalStateException::class,
    CancellationException::class,
  )
  suspend fun build(resolver: ProgrammableTransactionResolver): ProgrammableTransaction {
    val snapshotCommands = commands.toList()
    val mutableInputs = resolver.mutableInputIndexes(snapshotCommands)
    val resolvedInputs = resolveAllInputs(resolver, mutableInputs)
    return ProgrammableTransaction(resolvedInputs, snapshotCommands)
  }

  @Throws(IllegalArgumentException::class)
  fun build(@Suppress("UNUSED_PARAMETER") resolver: Any): ProgrammableTransaction = build()

  private suspend fun resolveAllInputs(
    resolver: ProgrammableTransactionResolver,
    mutableInputs: Set<Int>,
  ): List<CallArg> {
    val snapshotInputs = inputValues.toList()
    val idsToResolve =
      snapshotInputs
        .filterIsInstance<CallArg.ObjectStr>()
        .map { normalizeSuiObjectId(it.id) }
        .filter { !SYSTEM_ADDRESSES.contains(it) }
        .distinct()
    val objectsMap =
      if (idsToResolve.isEmpty()) {
        emptyMap()
      } else {
        resolver.resolveObjects(idsToResolve).entries.associate { (k, v) ->
          normalizeSuiObjectId(k) to v
        }
      }

    return snapshotInputs.withIndex().map { (index, callArg) ->
      if (callArg !is CallArg.ObjectStr) return@map callArg

      val normalizedId = normalizeSuiObjectId(callArg.id)
      if (SYSTEM_ADDRESSES.contains(normalizedId)) {
        CallArg.Object(
          ObjectArg.SharedObject(
            id = ObjectId(AccountAddress.fromString(normalizedId)),
            initialSharedVersion = 1L,
            mutable = index in mutableInputs,
          )
        )
      } else {
        val suiObject =
          objectsMap[normalizedId]
            ?: throw IllegalStateException(
              "Object ${callArg.id} not found on chain. (Normalized lookup: $normalizedId)"
            )
        val resolvedObjectArg =
          when (val owner = suiObject.owner) {
            is ResolvedObjectOwner.Shared ->
              ObjectArg.SharedObject(
                id = ObjectId(AccountAddress.fromString(suiObject.objectId)),
                initialSharedVersion = owner.initialSharedVersion,
                mutable = index in mutableInputs,
              )
            ResolvedObjectOwner.AddressOwner,
            ResolvedObjectOwner.Immutable -> {
              val digest =
                suiObject.digest
                  ?: throw IllegalStateException("Couldn't Resolve digest for ${callArg.id}")
              val version =
                suiObject.version
                  ?: throw IllegalStateException("Couldn't Resolve version for ${callArg.id}")
              ObjectArg.ImmOrOwnedObject(
                ObjectReference(
                  Reference(AccountAddress.fromString(suiObject.objectId)),
                  version,
                  ObjectDigest(Digest.fromString(digest)),
                )
              )
            }
          }
        CallArg.Object(resolvedObjectArg)
      }
    }
  }

  companion object {
    private val SYSTEM_ADDRESSES =
      setOf(
        normalizeSuiObjectId("0x5"),
        normalizeSuiObjectId("0x6"),
        normalizeSuiObjectId("0x8"),
        normalizeSuiObjectId("0x403"),
      )
  }
}

internal fun normalizeSuiObjectId(id: String): String {
  val clean = id.trim().removePrefix("0x").removePrefix("0X").lowercase()
  require(clean.isNotEmpty() && clean.length <= 64) { "Invalid Sui object ID length: $id" }
  require(clean.all { it in '0'..'9' || it in 'a'..'f' }) {
    "Invalid hex character in Sui object ID: $id"
  }
  return "0x${clean.padStart(64, '0')}"
}

interface ProgrammableTransactionResolver {
  suspend fun mutableInputIndexes(commands: List<Command>): Set<Int>

  suspend fun resolveObjects(objectIds: List<String>): Map<String, ResolvedObject>
}

data class ResolvedObject(
  val objectId: String,
  val version: Long?,
  val digest: String?,
  val owner: ResolvedObjectOwner,
)

sealed class ResolvedObjectOwner {
  data class Shared(val initialSharedVersion: Long) : ResolvedObjectOwner()

  data object AddressOwner : ResolvedObjectOwner()

  data object Immutable : ResolvedObjectOwner()
}

@Serializable
sealed class BuilderArg {
  @Serializable
  data class Pure(val data: ByteArray) : BuilderArg() {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false
      other as Pure
      return data.contentEquals(other.data)
    }

    override fun hashCode(): Int = data.contentHashCode()
  }

  @Serializable data class ForcedNonUniquePure(val index: Int) : BuilderArg()

  @Serializable data class ForcedNonUniqueObject(val index: Int) : BuilderArg()

  @Serializable data class ForcedNonUniqueWithdrawal(val index: Int) : BuilderArg()
}

internal val SUI_COIN_TYPE =
  TypeTag.Struct(StructTag(AccountAddress.fromString("0x2"), "sui", "SUI"))

@Throws(IllegalArgumentException::class)
fun ptb(block: PtbDsl.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val dsl = PtbDsl(builder)
  dsl.block()
  return builder.build()
}

@Throws(IllegalArgumentException::class)
fun hexStringToByteArray(hexString: String): ByteArray {
  val clean = hexString.removePrefix("0x").removePrefix("0X")
  require(clean.length % 2 == 0) { "Hex string must have an even length" }
  return ByteArray(clean.length / 2) { i ->
    val hi = hexNibble(clean[i * 2])
    val lo = hexNibble(clean[i * 2 + 1])
    ((hi shl 4) or lo).toByte()
  }
}

private fun hexNibble(c: Char): Int =
  when (c) {
    in '0'..'9' -> c - '0'
    in 'a'..'f' -> c - 'a' + 10
    in 'A'..'F' -> c - 'A' + 10
    else -> throw IllegalArgumentException("Invalid hex character: '$c'")
  }
