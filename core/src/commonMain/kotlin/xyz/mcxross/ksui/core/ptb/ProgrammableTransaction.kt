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

import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.extension.asIdParts
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.ObjectId
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.util.MAX_COMMANDS_IN_PTB

@Serializable(
  with =
    xyz.mcxross.ksui.core.serializer.ProgrammableTransactionSerializer::class
)
data class ProgrammableTransaction(
  val inputs: List<xyz.mcxross.ksui.core.model.CallArg>,
  val commands: List<Command>,
) : TransactionKind() {
  init {
    require(commands.size <= MAX_COMMANDS_IN_PTB) { "Maximum number of commands is 1024" }
    require(inputs.none { it is xyz.mcxross.ksui.core.model.CallArg.ObjectStr }) {
      "ProgrammableTransaction contains unresolved ObjectStr inputs. Call build(sui) first."
    }
  }
}

class ProgrammableTransactionBuilder : Command() {

  private val inputs: MutableMap<BuilderArg, xyz.mcxross.ksui.core.model.CallArg> = mutableMapOf()

  private fun normalize(id: String): String {
    val clean = id.removePrefix("0x")
    val padded = clean.padStart(64, '0')
    return "0x$padded"
  }

  private val SYSTEM_ADDRESSES =
    setOf(
      normalize("0x5"), // System State
      normalize("0x6"), // Clock
      normalize("0x8"), // Random
      normalize("0x403"), // DenyList
    )

  private fun addInput(arg: BuilderArg, value: xyz.mcxross.ksui.core.model.CallArg): Argument {
    var index = 0
    for (key in inputs.keys) {
      if (key == arg) {
        return Argument.Input(index.toUShort())
      }
      index++
    }
    inputs[arg] = value
    return Argument.Input((inputs.size - 1).toUShort())
  }

  fun input(bytes: ByteArray, forceSeparate: Boolean = false): Argument {
    val arg =
      if (forceSeparate) {
        BuilderArg.ForcedNonUniquePure(inputs.size)
      } else {
        BuilderArg.Pure(bytes)
      }
    return addInput(arg, xyz.mcxross.ksui.core.model.CallArg.Pure(data = bytes))
  }

  fun address(str: String): Argument {
    return input(AccountAddress.fromString(str).data)
  }

  fun address(account: Account): Argument {
    return address(account.address)
  }

  fun address(address: AccountAddress): Argument {
    return input(address.data)
  }

  inline fun <reified T> pure(value: T): Argument {
    return input(Bcs.encodeToByteArray(value))
  }

  fun pure(bytes: ByteArray): Argument {
    return input(bytes)
  }

  inline fun <reified T> input(value: T): Argument {
    if (value is xyz.mcxross.ksui.core.model.ObjectArg) {
      return `object`(value)
    }
    return input(Bcs.encodeToByteArray(value))
  }

  fun system(): Argument = `object`("0x5")

  fun clock(): Argument = `object`("0x6")

  fun random(): Argument = `object`("0x8")

  fun denyList(): Argument = `object`("0x403")

  fun `object`(id: String): Argument {
    val normalizedId = normalize(id)

    inputs.entries.forEachIndexed { index, entry ->
      val value = entry.value
      if (
        value is xyz.mcxross.ksui.core.model.CallArg.ObjectStr &&
          normalize(value.id) == normalizedId
      ) {
        return Argument.Input(index.toUShort())
      }
    }

    return addInput(
      BuilderArg.ForcedNonUniqueObject(inputs.size),
      xyz.mcxross.ksui.core.model.CallArg.ObjectStr(id),
    )
  }

  fun `object`(objectArg: xyz.mcxross.ksui.core.model.ObjectArg): Argument {
    return addInput(
      BuilderArg.ForcedNonUniqueObject(inputs.size),
      xyz.mcxross.ksui.core.model.CallArg.Object(objectArg),
    )
  }

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
    commands.add(MoveCall(moveCall))
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun transferObjects(objects: List<Argument>, address: Argument): Argument.Result {
    val command = TransferObjects(objects, address)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun splitCoins(coin: Argument, into: List<Argument>): List<Argument.NestedResult> {
    require(into.isNotEmpty()) { "The 'into' list of amounts cannot be empty." }
    val command = SplitCoins(coin, into)
    commands.add(command)
    val commandIndex = (commands.size - 1).toUShort()
    return into.indices.map { i -> Argument.NestedResult(commandIndex, i.toUShort()) }
  }

  fun mergeCoins(coin: Argument, coins: List<Argument>): Argument.Result {
    val command = MergeCoins(coin, coins)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun publish(bytes: List<List<Byte>>, dependencies: List<ObjectId>): Argument.Result {
    val command = Publish(bytes, dependencies)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun makeMoveVec(typeTag: TypeTag?, values: List<Argument>): Argument.Result {
    val command = MakeMoveVec(typeTag, values)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun upgrade(
    modules: List<List<Byte>>,
    dependencies: List<ObjectId>,
    packageId: ObjectId,
    upgradeTicket: Argument,
  ): Argument.Result {
    val command = Upgrade(modules, dependencies, packageId, upgradeTicket)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun build(): ProgrammableTransaction = ProgrammableTransaction(inputs.values.toList(), list)

  suspend fun build(resolver: ProgrammableTransactionResolver): ProgrammableTransaction {
    val mutableInputs = resolver.mutableInputIndexes(list)
    val resolvedInputs = resolveAllInputs(resolver, mutableInputs)
    return ProgrammableTransaction(resolvedInputs, list)
  }

  fun build(@Suppress("UNUSED_PARAMETER") resolver: Any): ProgrammableTransaction = build()

  private suspend fun resolveAllInputs(
    resolver: ProgrammableTransactionResolver,
    mutableInputs: Set<Int>,
  ): List<xyz.mcxross.ksui.core.model.CallArg> {
    val inputList = inputs.values.toList()
    val idsToResolve =
      inputList
        .filterIsInstance<xyz.mcxross.ksui.core.model.CallArg.ObjectStr>()
        .map { normalize(it.id) }
        .filter { !SYSTEM_ADDRESSES.contains(it) }
        .distinct()
    val objectsMap =
      if (idsToResolve.isEmpty()) emptyMap() else resolver.resolveObjects(idsToResolve)

    return inputList.withIndex().map { (index, callArg) ->
      if (callArg !is xyz.mcxross.ksui.core.model.CallArg.ObjectStr) return@map callArg

      val normalizedId = normalize(callArg.id)
      if (SYSTEM_ADDRESSES.contains(normalizedId)) {
        xyz.mcxross.ksui.core.model.CallArg.Object(
          xyz.mcxross.ksui.core.model.ObjectArg.SharedObject(
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
              xyz.mcxross.ksui.core.model.ObjectArg.SharedObject(
                id = ObjectId(AccountAddress.fromString(suiObject.objectId)),
                initialSharedVersion = owner.initialSharedVersion,
                mutable = index in mutableInputs,
              )
            ResolvedObjectOwner.AddressOwner -> {
              val digest =
                suiObject.digest
                  ?: throw IllegalStateException("Couldn't Resolve digest for ${callArg.id}")
              val version =
                suiObject.version
                  ?: throw IllegalStateException("Couldn't Resolve version for ${callArg.id}")
              xyz.mcxross.ksui.core.model.ObjectArg.ImmOrOwnedObject(
                xyz.mcxross.ksui.core.model.ObjectReference(
                  xyz.mcxross.ksui.core.model.Reference(
                    AccountAddress.fromString(suiObject.objectId)
                  ),
                  version,
                  xyz.mcxross.ksui.core.model.ObjectDigest(
                    xyz.mcxross.ksui.core.model.Digest.fromString(digest)
                  ),
                )
              )
            }
          }
        xyz.mcxross.ksui.core.model.CallArg.Object(resolvedObjectArg)
      }
    }
  }
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

    override fun hashCode(): Int {
      return data.contentHashCode()
    }
  }

  @Serializable data class ForcedNonUniquePure(val index: Int) : BuilderArg()

  @Serializable data class ForcedNonUniqueObject(val index: Int) : BuilderArg()
}

fun ptb(block: PtbDsl.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val dsl = PtbDsl(builder)
  dsl.block()
  return builder.build()
}

fun hexStringToByteArray(hexString: String): ByteArray {
  val cleanedHexString = hexString.removePrefix("0x").replace(Regex("[^0-9A-Fa-f]"), "")
  val len = cleanedHexString.length

  require(len % 2 == 0) { "Hex string must have an even length" }

  return ByteArray(len / 2) { i ->
    val index = i * 2
    cleanedHexString.substring(index, index + 2).toInt(16).toByte()
  }
}
