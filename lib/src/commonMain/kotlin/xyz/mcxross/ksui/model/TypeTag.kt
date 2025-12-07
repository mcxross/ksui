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
package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.serializer.TypeTagSerializer

/**
 * A sealed class representing a fully-qualified Move type. This provides a type-safe way to
 * represent types for transaction building, eliminating the need for raw strings.
 */
@Serializable(with = TypeTagSerializer::class)
sealed class TypeTag {
  /** Returns the canonical string representation of the Move type. */
  abstract override fun toString(): String

  @Serializable
  data object Bool : TypeTag() {
    override fun toString() = "bool"
  }

  @Serializable
  data object U8 : TypeTag() {
    override fun toString() = "u8"
  }

  @Serializable
  data object U16 : TypeTag() {
    override fun toString() = "u16"
  }

  @Serializable
  data object U32 : TypeTag() {
    override fun toString() = "u32"
  }

  @Serializable
  data object U64 : TypeTag() {
    override fun toString() = "u64"
  }

  @Serializable
  data object U128 : TypeTag() {
    override fun toString() = "u128"
  }

  @Serializable
  data object U256 : TypeTag() {
    override fun toString() = "u256"
  }

  @Serializable
  data object Address : TypeTag() {
    override fun toString() = "address"
  }

  @Serializable
  data object Signer : TypeTag() {
    override fun toString() = "signer"
  }

  /**
   * Represents a Move `vector<T>` type.
   *
   * @param elementType The `TypeTag` of the elements in the vector.
   */
  @Serializable
  data class Vector(val elementType: TypeTag) : TypeTag() {
    override fun toString() = "vector<${elementType}>"
  }

  /**
   * Represents a Move `struct` type.
   *
   * @param tag The `StructTag` containing the full identifier of the struct.
   */
  @Serializable
  data class Struct(val tag: StructTag) : TypeTag() {
    override fun toString() = tag.toString()
  }
}

@Serializable
data class StructTag(
  val address: AccountAddress,
  val module: String,
  val name: String,
  val typeParams: List<TypeTag> = emptyList(),
) {
  override fun toString(): String {
    val params =
      if (typeParams.isNotEmpty()) {
        "<${typeParams.joinToString(", ")}>"
      } else {
        ""
      }
    return "$address::$module::$name$params"
  }

  companion object {
    val STRING = StructTag(AccountAddress.fromString("0x1"), "string", "String")
  }
}
