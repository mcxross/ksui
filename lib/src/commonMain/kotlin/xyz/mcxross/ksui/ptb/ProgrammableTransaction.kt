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
import xyz.mcxross.ksui.generated.GetNormalizedMoveFunctionQuery
import xyz.mcxross.ksui.generated.fragment.RPC_MOVE_FUNCTION_FIELDS
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectId
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.serializer.AnySerializer

@Serializable
data class ProgrammableTransaction(
  val inputs: List<@Serializable(with = AnySerializer::class) Any>,
  val commands: List<@Serializable(with = AnySerializer::class) Any>,
) : TransactionKind()

// The builder now inherits from Command, exposing all command-building functions
// directly within the builder's scope.
class ProgrammableTransactionBuilder : Command() {
  // Reverted to MutableMap to allow for input overwriting/de-duplication, as requested.
  private val inputs: MutableMap<BuilderArg, CallArg> = mutableMapOf()

  /**
   * Adds a new input to the transaction and returns an Argument pointing to it. This logic allows
   * for overwriting existing inputs with the same BuilderArg key.
   *
   * @param arg The BuilderArg key for the input.
   * @param value The CallArg value of the input.
   * @return An Argument.Input referencing the input's index based on the map's size.
   */
  private fun addInput(arg: BuilderArg, value: CallArg): Argument {
    inputs[arg] = value
    // NOTE: When a key is overwritten, the map size does not change,
    // so this will return the index of the last unique item added.
    return Argument.Input((inputs.size - 1).toUShort())
  }

  /**
   * Adds a pure byte array as an input.
   *
   * @param bytes The raw byte array to be used as an input.
   * @param forceSeparate If true, treats this input as unique, even if its bytes are identical to
   *   another input.
   * @return An Argument.Input referencing the new input.
   */
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

  fun address(address: AccountAddress): Argument {
    return input(address.data)
  }

  inline fun <reified T> pure(value: T): Argument {
    return input(Bcs.encodeToByteArray(value))
  }

  /**
   * A generic input function that serializes a given value into bytes using BCS. If the value is an
   * ObjectArg, it correctly routes to the `object` function.
   *
   * @param value The value to be serialized and added as an input.
   * @return An Argument.Input referencing the new input.
   */
  inline fun <reified T> input(value: T): Argument {
    if (value is ObjectArg) {
      return `object`(value)
    }
    return input(Bcs.encodeToByteArray(value))
  }

  fun `object`(id: String): Argument {
    return addInput(BuilderArg.ForcedNonUniqueObject(inputs.size), CallArg.ObjectStr(id))
  }

  /**
   * Adds a Sui object as an input. All object inputs are treated as unique inputs, but using the
   * same key means they will overwrite each other.
   *
   * @param objectArg The ObjectArg representing a Sui object.
   * @return An Argument.Input referencing the new object input.
   */
  fun `object`(objectArg: ObjectArg): Argument {
    // Using a unique key for each object to prevent overwriting.
    return addInput(BuilderArg.ForcedNonUniqueObject(inputs.size), CallArg.Object(objectArg))
  }

  private suspend fun gatherCommandMetadata(sui: Sui): Map<Int, Boolean> {
    val mutabilityMap = mutableMapOf<Int, Boolean>()
    val functionSignatureCache = mutableMapOf<String, GetNormalizedMoveFunctionQuery.Data?>()

    for (command in list) {
      when (command) {
        is MoveCall -> {
          val callDetails = command.moveCall
          val packageId = callDetails.pakage.hash.toString()
          val module = callDetails.module
          val function = callDetails.function
          val target = "$packageId::$module::$function"

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
    }
    return mutabilityMap
  }

  /**
   * Performs the unified resolution of all inputs, turning ObjectStr into fully-formed ObjectArgs.
   * This works for inputs from ANY command (MoveCall, TransferObjects, etc.).
   */
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

  /**
   * Asynchronously builds the final ProgrammableTransaction by orchestrating metadata gathering and
   * input resolution.
   */
  suspend fun build(sui: Sui): ProgrammableTransaction {
    val mutabilityMap = gatherCommandMetadata(sui)

    val resolvedInputs = resolveAllInputs(sui, mutabilityMap)

    return ProgrammableTransaction(resolvedInputs, list)
  }
}

/**
 * A sealed class representing the different kinds of builder arguments, used as keys in the input
 * map to allow for de-duplication.
 */
@Serializable
sealed class BuilderArg {
  /** A key for a pure byte array input. Equality is based on the byte array content. */
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

  /** A key for a pure input that should not be de-duplicated. Equality is based on its index. */
  @Serializable data class ForcedNonUniquePure(val index: Int) : BuilderArg()

  /** A key for an object input that should not be de-duplicated. */
  @Serializable data class ForcedNonUniqueObject(val index: Int) : BuilderArg()
}

/**
 * A DSL for building a [ProgrammableTransaction].
 *
 * @param block The builder block to configure the transaction.
 * @return The constructed [ProgrammableTransaction].
 */
suspend fun ptb(
  client: Sui = SuiKit.client,
  block: ProgrammableTransactionBuilder.() -> Unit,
): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  builder.block()
  return builder.build(client)
}

/**
 * Utility function to convert a hex string to a byte array.
 *
 * @param hexString The hex string (e.g., "0x123...").
 * @return The corresponding ByteArray.
 */
fun hexStringToByteArray(hexString: String): ByteArray {
  val cleanedHexString = hexString.removePrefix("0x").replace(Regex("[^0-9A-Fa-f]"), "")
  val len = cleanedHexString.length

  require(len % 2 == 0) { "Hex string must have an even length" }

  return ByteArray(len / 2) { i ->
    val index = i * 2
    cleanedHexString.substring(index, index + 2).toInt(16).toByte()
  }
}
