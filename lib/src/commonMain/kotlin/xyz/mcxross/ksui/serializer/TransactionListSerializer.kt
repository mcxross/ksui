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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.ptb.TransactionKind

object TransactionListSerializer : KSerializer<List<TransactionKind>> {
  override val descriptor: SerialDescriptor =
    ListSerializer(TransactionKind.serializer()).descriptor

  override fun serialize(encoder: Encoder, value: List<TransactionKind>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      kotlinx.serialization.json.JsonArray(
        value.map { tk ->
          when (tk) {
            is TransactionKind.MoveCall -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.Publish -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.PaySui -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.PayAllSui -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.Transfer -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.TransferSui -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.SplitCoin -> kotlinx.serialization.json.buildJsonObject {}
            else -> {
              kotlinx.serialization.json.buildJsonObject {}
            }
          }
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<TransactionKind> {
    require(decoder is JsonDecoder)
    val jsonArray = decoder.decodeJsonElement().jsonArray
    return jsonArray.map { jsonElement ->
      val jsonObject = jsonElement.jsonObject
      if (jsonObject.containsKey("MoveCall")) {
        val moveCallObject = jsonObject["MoveCall"]?.jsonObject
        TransactionKind.MoveCall(
          moveCallObject?.get("package")?.jsonPrimitive?.content ?: "ksui-default",
          moveCallObject?.get("module")?.jsonPrimitive?.content ?: "ksui-default",
          moveCallObject?.get("function")?.jsonPrimitive?.content ?: "ksui-default",
          moveCallObject?.get("type_arguments")?.jsonArray?.map { it.jsonPrimitive.content },
        )
      } else if (jsonObject.containsKey("SplitCoins")) {
        val splitCoinsObject = jsonObject["SplitCoins"]?.jsonArray
        TransactionKind.SplitCoin(emptyList())
      } else {
        TransactionKind.DefaultTransaction("default")
      }
    }
  }
}
