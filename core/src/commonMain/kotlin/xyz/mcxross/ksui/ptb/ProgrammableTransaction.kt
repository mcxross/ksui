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
package xyz.mcxross.ksui.ptb

import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.extension.asIdParts
import xyz.mcxross.ksui.model.*
import xyz.mcxross.ksui.serializer.ProgrammableTransactionSerializer
import xyz.mcxross.ksui.util.MAX_COMMANDS_IN_PTB

@Serializable(with = ProgrammableTransactionSerializer::class)
data class ProgrammableTransaction(val inputs: List<CallArg>, val commands: List<Command>) :
  TransactionKind() {
  init {
    require(commands.size <= MAX_COMMANDS_IN_PTB) { "Maximum number of commands is 1024" }
    require(inputs.none { it is CallArg.ObjectStr }) {
      "ProgrammableTransaction contains unresolved ObjectStr inputs. Call build(sui) first."
    }
  }
}

class ProgrammableTransactionBuilder : Command() {

  private val inputs: MutableMap<BuilderArg, CallArg> = mutableMapOf()

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

  private fun addInput(arg: BuilderArg, value: CallArg): Argument {
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
    return addInput(arg, CallArg.Pure(data = bytes))
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
    if (value is ObjectArg) {
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
      if (value is CallArg.ObjectStr && normalize(value.id) == normalizedId) {
        return Argument.Input(index.toUShort())
      }
    }

    return addInput(BuilderArg.ForcedNonUniqueObject(inputs.size), CallArg.ObjectStr(id))
  }

  fun `object`(objectArg: ObjectArg): Argument {
    return addInput(BuilderArg.ForcedNonUniqueObject(inputs.size), CallArg.Object(objectArg))
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
    commands.add(Command.MoveCall(moveCall))
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun transferObjects(objects: List<Argument>, address: Argument): Argument.Result {
    val command = Command.TransferObjects(objects, address)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun splitCoins(coin: Argument, into: List<Argument>): List<Argument.NestedResult> {
    require(into.isNotEmpty()) { "The 'into' list of amounts cannot be empty." }
    val command = Command.SplitCoins(coin, into)
    commands.add(command)
    val commandIndex = (commands.size - 1).toUShort()
    return into.indices.map { i -> Argument.NestedResult(commandIndex, i.toUShort()) }
  }

  fun mergeCoins(coin: Argument, coins: List<Argument>): Argument.Result {
    val command = Command.MergeCoins(coin, coins)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun publish(bytes: List<List<Byte>>, dependencies: List<ObjectId>): Argument.Result {
    val command = Command.Publish(bytes, dependencies)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun makeMoveVec(typeTag: TypeTag?, values: List<Argument>): Argument.Result {
    val command = Command.MakeMoveVec(typeTag, values)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun upgrade(
    modules: List<List<Byte>>,
    dependencies: List<ObjectId>,
    packageId: ObjectId,
    upgradeTicket: Argument,
  ): Argument.Result {
    val command = Command.Upgrade(modules, dependencies, packageId, upgradeTicket)
    commands.add(command)
    return Argument.Result((commands.size - 1).toUShort())
  }

  fun build(): ProgrammableTransaction = ProgrammableTransaction(inputs.values.toList(), list)

  fun build(@Suppress("UNUSED_PARAMETER") resolver: Any): ProgrammableTransaction = build()
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
