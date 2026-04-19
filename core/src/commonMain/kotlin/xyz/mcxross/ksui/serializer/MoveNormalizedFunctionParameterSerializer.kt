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
package xyz.mcxross.ksui.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.model.MoveFunctionParameter

object MoveNormalizedFunctionParameterSerializer : KSerializer<MoveFunctionParameter> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("MoveFunctionParameter", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: MoveFunctionParameter) {
    require(encoder is JsonEncoder)
  }

  override fun deserialize(decoder: Decoder): MoveFunctionParameter {
    require(decoder is JsonDecoder)
    return when (val jsonElement = decoder.decodeJsonElement()) {
      is JsonObject -> {
        if (jsonElement.containsKey("MutableReference")) {
          MoveFunctionParameter.MutableReference()
        } else if (jsonElement.containsKey("Struct")) {
          val struct = jsonElement["Struct"]?.jsonObject
          MoveFunctionParameter.Struct(
            struct?.get("address")?.jsonPrimitive?.content ?: "",
            struct?.get("module")?.jsonPrimitive?.content ?: "",
            struct?.get("name")?.jsonPrimitive?.content ?: "",
          )
        } else if (jsonElement.containsKey("Vector")) {
          if (jsonElement["Vector"] is JsonObject) {
            MoveFunctionParameter.Vector(
              if (jsonElement["Vector"]?.jsonObject?.containsKey("U8") == true) {
                MoveFunctionParameter.U8()
              } else if (jsonElement["Vector"]?.jsonObject?.containsKey("U64") == true) {
                MoveFunctionParameter.U64()
              } else if (jsonElement["Vector"]?.jsonObject?.containsKey("U128") == true) {
                MoveFunctionParameter.U128()
              } else if (jsonElement["Vector"]?.jsonObject?.containsKey("U256") == true) {
                MoveFunctionParameter.U256()
              } else {
                MoveFunctionParameter.Undefined()
              }
            )
          } else if (jsonElement["Vector"] is JsonPrimitive) {
            MoveFunctionParameter.Undefined()
          } else {
            MoveFunctionParameter.Undefined()
          }
        } else {
          MoveFunctionParameter.Undefined()
        }
      }
      is JsonPrimitive -> {
        if (jsonElement.isString) {
          when (jsonElement.jsonPrimitive.content) {
            "Address" -> {
              MoveFunctionParameter.Address()
            }
            "U64" -> {
              MoveFunctionParameter.U64()
            }
            else -> {
              MoveFunctionParameter.Undefined()
            }
          }
        } else {
          MoveFunctionParameter.Undefined()
        }
      }
      else -> {
        MoveFunctionParameter.Undefined()
      }
    }
  }
}
