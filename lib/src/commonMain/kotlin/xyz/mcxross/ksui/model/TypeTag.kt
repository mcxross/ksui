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
package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.serializer.TypeTagSerializer

@Serializable(with = TypeTagSerializer::class)
sealed class TypeTag {
  abstract fun asMoveType(): String

  object U8 : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "u8"
  }

  object U16 : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "u16"
  }

  object U32 : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "u32"
  }

  object U64 : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "u64"
  }

  object U128 : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "u128"
  }

  object U256 : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "u256"
  }

  object Bool : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "bool"
  }

  object Address : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "address"
  }

  class Vector : TypeTag() {

    private var of: TypeTag? = null

    fun of(typeTag: TypeTag): Vector {
      of = typeTag
      return this
    }

    override fun asMoveType(): String = toString()

    override fun toString(): String = "vector<$of>"
  }

  @Serializable
  data class Struct(
    val address: AccountAddress,
    val module: Identifier,
    val name: Identifier,
    val typeParams: List<TypeTag> = emptyList(),
  ) : TypeTag() {
    override fun asMoveType(): String = toString()

    override fun toString(): String = "struct<$address::${module}::${name}>"

    companion object {
      fun from(str: String): Struct {
        val parts = str.split("::")
        require(parts.size == 3) { "Invalid struct type tag: $str" }
        return Struct(AccountAddress.fromString(parts[0]), parts[1], parts[2])
      }
    }
  }
}
