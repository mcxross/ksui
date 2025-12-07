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
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.extension.asIdParts
import xyz.mcxross.ksui.generated.GetNormalizedMoveFunctionQuery
import xyz.mcxross.ksui.generated.fragment.RPC_MOVE_FUNCTION_FIELDS
import xyz.mcxross.ksui.model.*
import xyz.mcxross.ksui.serializer.AnySerializer

@Serializable
data class ProgrammableTransaction(
  val inputs: List<@Serializable(with = AnySerializer::class) Any>,
  val commands: List<@Serializable(with = AnySerializer::class) Any>,
) : TransactionKind()

class ProgrammableTransactionBuilder : Command() {

  private val inputs: MutableMap<BuilderArg, CallArg> = mutableMapOf()

  private fun addInput(arg: BuilderArg, value: CallArg): Argument {
    // 1. Check if this argument already exists in the map
    // distinct inputs are keys in the LinkedHashMap, so iteration order is preserved (0, 1, 2...)
    var index = 0
    for (key in inputs.keys) {
      if (key == arg) {
        return Argument.Input(index.toUShort())
      }
      index++
    }

    // 2. If it's new, add it to the map
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

  private suspend fun gatherCommandMetadata(sui: Sui): Map<Int, Boolean> {
    val mutabilityMap = mutableMapOf<Int, Boolean>()
    val functionSignatureCache = mutableMapOf<String, GetNormalizedMoveFunctionQuery.Data?>()
    for (command in list) {
      if (command is Command.MoveCall) {
        val callDetails = command.moveCall
        val target = "${callDetails.pakage.hash}::${callDetails.module}::${callDetails.function}"
        val signatureResponse =
          functionSignatureCache.getOrPut(target) {
            when (val result = sui.getNormalizedMoveFunction(target)) {
              is Result.Ok -> result.value
              is Result.Err ->
                throw IllegalStateException("Failed to get function signature for $target")
            }
          }
        val params: List<RPC_MOVE_FUNCTION_FIELDS.Parameter>? =
          signatureResponse
            ?.`object`
            ?.asMovePackage
            ?.module
            ?.function
            ?.rPC_MOVE_FUNCTION_FIELDS
            ?.parameters
        if (params == null) {
          continue
        }
        command.moveCall.arguments.zip(params).forEach { (argument, parameter) ->
          if (argument is Argument.Input) {
            val signatureMap = parameter.signature as? Map<*, *>
            val ref = signatureMap?.get("ref") as? String
            if (ref == "&mut") {
              mutabilityMap[argument.index.toInt()] = true
            }
          }
        }
      }
    }
    return mutabilityMap
  }

  private suspend fun resolveAllInputs(sui: Sui, mutabilityMap: Map<Int, Boolean>): List<CallArg> {
    val inputList = inputs.values.toList()
    return inputList.withIndex().map { (index, callArg) ->
      if (callArg is CallArg.ObjectStr) {
        val objResult = sui.getObject(callArg.id, ObjectDataOptions(showOwner = true))
        when (objResult) {
          is Result.Ok -> {
            val suiObject = objResult.value?.`object`
            val owner = suiObject?.rPC_OBJECT_FIELDS?.owner?.rPC_OBJECT_OWNER_FIELDS
            val objectId =
              suiObject?.rPC_OBJECT_FIELDS?.objectId
                ?: throw IllegalStateException("Object data is missing objectId")

            val resolvedObjectArg =
              when (owner?.__typename) {
                "Shared" -> {
                  val isMutable = mutabilityMap[index] ?: false
                  ObjectArg.SharedObject(
                    id = ObjectId(AccountAddress.fromString(objectId.toString())),
                    initialSharedVersion =
                      owner.onShared?.initialSharedVersion?.toString()?.toLong() ?: 0L,
                    mutable = isMutable,
                  )
                }
                "AddressOwner" -> {
                  if (suiObject.rPC_OBJECT_FIELDS.digest.isNullOrEmpty()) {
                    throw IllegalStateException("Couldn't Resolve digest")
                  }
                  ObjectArg.ImmOrOwnedObject(
                    ObjectReference(
                      Reference(
                        AccountAddress.fromString(suiObject.rPC_OBJECT_FIELDS.objectId.toString())
                      ),
                      suiObject.rPC_OBJECT_FIELDS.version.toString().toLong(),
                      ObjectDigest(Digest.fromString(suiObject.rPC_OBJECT_FIELDS.digest)),
                    )
                  )
                }
                else ->
                  throw IllegalStateException("Unsupported object owner type: ${owner?.__typename}")
              }
            CallArg.Object(resolvedObjectArg)
          }
          is Result.Err ->
            throw IllegalStateException("Failed to fetch object details for ${callArg.id}")
        }
      } else {
        callArg
      }
    }
  }

  suspend fun build(sui: Sui): ProgrammableTransaction {
    val mutabilityMap = gatherCommandMetadata(sui)
    val resolvedInputs = resolveAllInputs(sui, mutabilityMap)
    return ProgrammableTransaction(resolvedInputs, list)
  }
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

suspend fun ptb(client: Sui = SuiKit.client, block: PtbDsl.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val dsl = PtbDsl(builder)
  dsl.block()
  return builder.build(client)
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
