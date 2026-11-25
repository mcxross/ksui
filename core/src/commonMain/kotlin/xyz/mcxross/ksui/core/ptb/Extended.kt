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
package xyz.mcxross.ksui.core.ptb

import xyz.mcxross.bcs.Bcs
import xyz.mcxross.bcs.stream.BcsDataOutputBuffer
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.StructTag
import xyz.mcxross.ksui.core.model.TypeTag

private val PACKAGE_ONE = AccountAddress.fromString("0x1")
private val PACKAGE_TWO = AccountAddress.fromString("0x2")

private fun StructTag.isStdString(): Boolean =
  address == PACKAGE_ONE &&
    typeParams.isEmpty() &&
    ((module == "string" && name == "String") || (module == "ascii" && name == "String"))

private fun StructTag.isSuiObjectId(): Boolean =
  address == PACKAGE_TWO && typeParams.isEmpty() && module == "object" && name == "ID"

fun isPure(type: TypeTag): Boolean =
  when (type) {
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
    is TypeTag.Struct -> type.tag.isStdString() || type.tag.isSuiObjectId()
  }

@OptIn(ExperimentalUnsignedTypes::class)
private fun bcsEncodePureValue(type: TypeTag, value: Any): ByteArray {
  val output = BcsDataOutputBuffer()

  when (type) {
    is TypeTag.Vector -> {
      require(value is List<*>) { "Value for a vector must be a List" }
      val elementType = type.elementType
      output.writeULEB128(value.size)
      value.forEach { element ->
        requireNotNull(element) { "Vector pure elements must not be null" }
        output.addAll(bcsEncodePureValue(elementType, element))
      }
    }
    is TypeTag.Bool -> output.addAll(Bcs.encodeToByteArray(value as Boolean))
    is TypeTag.U8 -> output.addAll(Bcs.encodeToByteArray(value as UByte))
    is TypeTag.U16 -> output.addAll(Bcs.encodeToByteArray(value as UShort))
    is TypeTag.U32 -> output.addAll(Bcs.encodeToByteArray(value as UInt))
    is TypeTag.U64 -> output.addAll(Bcs.encodeToByteArray(value as ULong))
    is TypeTag.U128 -> output.addAll(Bcs.encodeToByteArray(value as UByteArray))
    is TypeTag.U256 -> output.addAll(Bcs.encodeToByteArray(value as UByteArray))
    is TypeTag.Address -> output.addAll(AccountAddress.fromString(value.toString()).data)
    is TypeTag.Struct ->
      when {
        type.tag.isStdString() -> output.addAll(Bcs.encodeToByteArray(value as String))
        type.tag.isSuiObjectId() -> output.addAll(AccountAddress.fromString(value.toString()).data)
        else ->
          throw IllegalArgumentException(
            "Cannot serialize non-pure struct ${type.tag} as a pure value.",
          )
      }
    else -> throw IllegalArgumentException("Unsupported pure type for serialization: $type")
  }

  return output.toByteArray()
}

internal fun ProgrammableTransactionBuilder.generic(type: TypeTag, value: Any): Argument {
  return if (isPure(type)) {
    pure(bcsEncodePureValue(type, value))
  } else {
    when (type) {
      is TypeTag.Vector -> {
        require(value is List<*>) { "Value for a vector type must be a List." }
        val elementType = type.elementType
        val mappedValues = value.map {
          requireNotNull(it) { "Vector elements must not be null" }
          generic(elementType, it)
        }
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

fun ProgrammableTransactionBuilder.arg(value: ULong): Argument = generic(TypeTag.U64, value)

fun ProgrammableTransactionBuilder.arg(value: String): Argument =
  generic(TypeTag.Struct(StructTag.STRING), value)

fun ProgrammableTransactionBuilder.arg(value: AccountAddress): Argument =
  generic(TypeTag.Address, value)

@Throws(IllegalArgumentException::class, ClassCastException::class)
fun <T> ProgrammableTransactionBuilder.arg(values: List<T>, elementType: TypeTag): Argument =
  generic(TypeTag.Vector(elementType), values)
