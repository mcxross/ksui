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
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.serializer.AnySerializer

@Serializable
data class ProgrammableTransaction(
  val inputs: List<@Serializable(with = AnySerializer::class) Any>,
  val commands: List<@Serializable(with = AnySerializer::class) Any>,
) : TransactionKind()

// The builder now inherits from Command, exposing all command-building functions
// directly within the builder's scope.
class ProgrammableTransactionBuilder : Command() {
  // Use a MutableList instead of a Map to allow for multiple, non-unique inputs,
  // especially for objects which were previously overwriting each other.
  private val inputs: MutableList<CallArg> = mutableListOf()

  /**
   * Adds a new input to the transaction and returns an Argument pointing to it.
   *
   * @param value The CallArg to add as an input.
   * @return An Argument.Input referencing the newly added input's index.
   */
  private fun addInput(value: CallArg): Argument {
    inputs.add(value)
    return Argument.Input((inputs.size - 1).toUShort())
  }

  /**
   * Adds a pure byte array as an input.
   *
   * @param bytes The raw byte array to be used as an input.
   * @return An Argument.Input referencing the new input.
   */
  fun input(bytes: ByteArray): Argument {
    return addInput(CallArg.Pure(data = bytes))
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

  /**
   * Adds a Sui object as an input. This is the corrected way to handle multiple object inputs.
   *
   * @param objectArg The ObjectArg representing a Sui object.
   * @return An Argument.Input referencing the new object input.
   */
  fun `object`(objectArg: ObjectArg): Argument {
    return addInput(CallArg.Object(objectArg))
  }

  /**
   * Builds the final ProgrammableTransaction.
   *
   * @return A constructed ProgrammableTransaction.
   */
  fun build(): ProgrammableTransaction {
    // The `list` property containing the commands is inherited from the Command class.
    return ProgrammableTransaction(inputs.toList(), list)
  }
}

/**
 * A DSL for building a [ProgrammableTransaction].
 *
 * @param block The builder block to configure the transaction.
 * @return The constructed [ProgrammableTransaction].
 */
fun ptb(block: ProgrammableTransactionBuilder.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  builder.block()
  return builder.build()
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
