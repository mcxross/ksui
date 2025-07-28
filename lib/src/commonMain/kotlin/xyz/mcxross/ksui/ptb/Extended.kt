/*
 * Copyright 2025 McXross
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
package xyz.mcxross.ksui.ptb

import xyz.mcxross.bcs.Bcs
import xyz.mcxross.bcs.stream.BcsDataOutputBuffer
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.StructTag
import xyz.mcxross.ksui.model.TypeTag

fun isPure(type: TypeTag): Boolean {
  return when (type) {
    is TypeTag.Bool,
    is TypeTag.U8,
    is TypeTag.U16,
    is TypeTag.U32,
    is TypeTag.U64,
    is TypeTag.U128,
    is TypeTag.U256,
    is TypeTag.Address,
    is TypeTag.Signer -> true

    is TypeTag.Vector -> isPure(type.elementType)

    is TypeTag.Struct ->
      when (type.tag.toString()) {
        "0x1::string::String",
        "0x1::ascii::String",
        "0x2::object::ID" -> true
        else -> false
      }
  }
}

/**
 * Recursively serializes a pure value according to its TypeTag. This correctly handles nested pure
 * vectors.
 *
 * @param type The `TypeTag` of the value to serialize.
 * @param value The value itself.
 * @return A single `ByteArray` representing the value, ready for a `pure()` call.
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun bcsEncodePureValue(type: TypeTag, value: Any): ByteArray {
  val bcs = Bcs {}
  val output = BcsDataOutputBuffer()

  when (type) {
    is TypeTag.Vector -> {
      require(value is List<*>) { "Value for a vector must be a List" }
      val elementType = type.elementType

      output.writeULEB128(value.size)

      value.forEach { element -> output.addAll(bcsEncodePureValue(elementType, element!!)) }
    }

    is TypeTag.Bool -> output.addAll(bcs.encodeToByteArray(value as Boolean))
    is TypeTag.U8 -> output.addAll(bcs.encodeToByteArray(value as UByte))
    is TypeTag.U16 -> output.addAll(bcs.encodeToByteArray(value as UShort))
    is TypeTag.U32 -> output.addAll(bcs.encodeToByteArray(value as UInt))
    is TypeTag.U64 -> output.addAll(bcs.encodeToByteArray(value as ULong))
    is TypeTag.U128 -> output.addAll(bcs.encodeToByteArray(value as UByteArray))
    is TypeTag.U256 -> output.addAll(bcs.encodeToByteArray(value as UByteArray))
    is TypeTag.Address -> output.addAll(AccountAddress.fromString(value.toString()).data)
    is TypeTag.Struct -> {
      when (type.tag.toString()) {
        "0x1::string::String",
        "0x1::ascii::String" -> output.addAll(bcs.encodeToByteArray(value as String))
        "0x2::object::ID" -> output.addAll(bcs.encodeToByteArray(value as String))
        else ->
          throw IllegalArgumentException(
            "Cannot serialize non-pure struct ${type.tag} as a pure value."
          )
      }
    }
    else -> throw IllegalArgumentException("Unsupported pure type for serialization: $type")
  }

  return output.toByteArray()
}

/**
 * A smart constructor that creates the correct argument type based on the Move type. This is an
 * extension function on `ProgrammableTransactionBuilder` to give it access to the `pure`, `object`,
 * and `makeMoveVec` functions.
 *
 * @param type The strongly-typed `TypeTag` of the argument.
 * @param value The value for the argument.
 * @return An `Argument` that can be used in a command.
 */
internal fun ProgrammableTransactionBuilder.generic(type: TypeTag, value: Any): Argument {
  return if (isPure(type)) {
    pure(bcsEncodePureValue(type, value))
  } else {
    when (type) {
      is TypeTag.Vector -> {
        require(value is List<*>) { "Value for a vector type must be a List." }
        val elementType = type.elementType

        val mappedValues = value.map { generic(elementType, it!!) }

        makeMoveVec(typeTag = elementType, values = mappedValues)
      }
      else -> {
        require(value is String) { "Value for an object type must be an object ID string." }
        `object`(value)
      }
    }
  }
}

fun ProgrammableTransactionBuilder.arg(value: Boolean): Argument = generic(TypeTag.Bool, value)

fun ProgrammableTransactionBuilder.arg(value: UByte): Argument = generic(TypeTag.U8, value)

fun ProgrammableTransactionBuilder.arg(value: UShort): Argument = generic(TypeTag.U16, value)

fun ProgrammableTransactionBuilder.arg(value: UInt): Argument = generic(TypeTag.U32, value)

/** Creates a transaction argument from a ULong, correctly identifying it as a `u64`. */
fun ProgrammableTransactionBuilder.arg(value: ULong): Argument = generic(TypeTag.U64, value)

/**
 * Creates a transaction argument from a String, correctly identifying it as a
 * `0x1::string::String`.
 */
fun ProgrammableTransactionBuilder.arg(value: String): Argument =
  generic(TypeTag.Struct(StructTag.STRING), value)

/**
 * Creates a transaction argument from an AccountAddress, correctly identifying it as an `address`.
 */
fun ProgrammableTransactionBuilder.arg(value: AccountAddress): Argument =
  generic(TypeTag.Address, value)

/**
 * A smart constructor for lists that is now fully type-safe. It automatically decides whether to
 * use `pure` (for pure vectors) or `makeMoveVec` (for object vectors).
 *
 * @param values The list of values for the vector.
 * @param elementType The strongly-typed `TypeTag` of the vector's elements.
 */
fun <T> ProgrammableTransactionBuilder.arg(values: List<T>, elementType: TypeTag): Argument {
  return generic(TypeTag.Vector(elementType), values)
}
