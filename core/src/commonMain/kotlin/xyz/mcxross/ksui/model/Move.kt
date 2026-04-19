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
import xyz.mcxross.ksui.serializer.MoveFunctionArgTypeSerializer
import xyz.mcxross.ksui.serializer.MoveNormalizedFunctionParameterSerializer

@Serializable(with = MoveFunctionArgTypeSerializer::class)
abstract class MoveFunctionArgType {
  @Serializable data class MoveFunctionArgDefault(val default: String) : MoveFunctionArgType()

  @Serializable data class MoveFunctionArgObject(val objekt: String) : MoveFunctionArgType()

  @Serializable data class MoveFunctionArgString(val str: String) : MoveFunctionArgType()
}

@Serializable(with = MoveNormalizedFunctionParameterSerializer::class)
abstract class MoveFunctionParameter {

  @Serializable
  class U8 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U8"
    }
  }

  @Serializable
  class U64 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U64"
    }
  }

  @Serializable
  class U128 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U128"
    }
  }

  @Serializable
  class U256 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U256"
    }
  }

  @Serializable
  class Undefined : MoveFunctionParameter() {
    override fun toString(): String {
      return "Undefined"
    }
  }

  @Serializable
  data class Struct(val address: String, val module: String, val name: String) :
    MoveFunctionParameter()

  @Serializable
  class MutableReference : MoveFunctionParameter() {
    val struct: Struct? = null

    override fun toString(): String {
      return "MutableReference"
    }

    override fun hashCode(): Int {
      return struct?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false

      other as MutableReference

      if (struct != other.struct) return false

      return true
    }
  }

  @Serializable data class Vector(val of: MoveFunctionParameter) : MoveFunctionParameter()

  @Serializable
  class Address : MoveFunctionParameter() {
    override fun toString(): String {
      return "Address"
    }
  }

  override fun toString(): String {
    return "MoveFunctionParameter"
  }
}
