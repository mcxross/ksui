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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import xyz.mcxross.ksui.model.DataTransactionInput

object TransactionInputListSerializer : KSerializer<List<DataTransactionInput>> {

  override val descriptor: SerialDescriptor =
    ListSerializer(DataTransactionInput.serializer()).descriptor

  override fun serialize(encoder: Encoder, value: List<DataTransactionInput>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      kotlinx.serialization.json.JsonArray(
        value.map { dti ->
          when (dti) {
            is DataTransactionInput.DtiImmOrOwnedObject ->
              kotlinx.serialization.json.buildJsonObject {
                put("type", dti.type)
                put("objectType", dti.objectType)
                put("objectId", dti.objectId)
                put("version", dti.version)
                put("digest", dti.digest)
              }
            is DataTransactionInput.DtiPure ->
              kotlinx.serialization.json.buildJsonObject {
                put("type", dti.type)
                put("valueType", dti.valueType)
                put("value", encoder.json.parseToJsonElement(dti.value!!))
              }
            is DataTransactionInput.DtiSharedObject ->
              kotlinx.serialization.json.buildJsonObject {
                put("type", dti.type)
                put("objectType", dti.objectType)
                put("objectId", dti.objectId)
                put("initialSharedVersion", dti.initialSharedVersion)
                put("mutable", dti.mutable)
              }
            is DataTransactionInput.DtiDefault ->
              kotlinx.serialization.json.buildJsonObject { put("type", dti.type) }
            else -> {
              kotlinx.serialization.json.buildJsonObject { put("type", "default") }
            }
          }
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<DataTransactionInput> {
    require(decoder is JsonDecoder)
    val jsonArray = decoder.decodeJsonElement().jsonArray
    return jsonArray.map { jsonElement ->
      val jsonObject = jsonElement.jsonObject
      when (val type = jsonObject["objectType"]?.jsonPrimitive?.content) {
        "immOrOwnedObject" ->
          DataTransactionInput.DtiImmOrOwnedObject(
            type = type,
            objectType = jsonObject["objectType"]?.jsonPrimitive?.content!!,
            objectId = jsonObject["objectId"]?.jsonPrimitive?.content!!,
            version = jsonObject["version"]?.jsonPrimitive?.long!!,
            digest = jsonObject["digest"]?.jsonPrimitive?.content!!,
          )
        "sharedObject" ->
          DataTransactionInput.DtiSharedObject(
            type = type,
            objectType = jsonObject["objectType"]?.jsonPrimitive?.content!!,
            objectId = jsonObject["objectId"]?.jsonPrimitive?.content!!,
            initialSharedVersion = jsonObject["initialSharedVersion"]?.jsonPrimitive?.long!!,
            mutable = jsonObject["mutable"]?.jsonPrimitive?.boolean!!,
          )
        else ->
          if ((jsonObject["type"]?.jsonPrimitive?.content ?: "") == "pure") {
            DataTransactionInput.DtiPure(
              type = "pure",
              valueType = jsonObject["valueType"]?.jsonPrimitive?.content!!,
              value = jsonObject["value"]?.toString(),
            )
          } else {
            DataTransactionInput.DtiDefault(type = "default")
          }
      }
    }
  }
}
