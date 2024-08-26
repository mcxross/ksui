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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import xyz.mcxross.ksui.model.TransactionFilter

object TransactionFilterSerializer : KSerializer<TransactionFilter> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TransactionFilter") {}

  override fun serialize(encoder: Encoder, value: TransactionFilter) {
    require(encoder is JsonEncoder)
    val json =
      when (value) {
        is TransactionFilter.Checkpoint ->
          buildJsonObject { put("Checkpoint", value.checkpointSequenceNumber.toString()) }
        is TransactionFilter.MoveFunction ->
          buildJsonObject {
            putJsonObject("MoveFunction") {
              //put("package", value.pakage.hash)
              put("module", value.module)
              put("function", value.function)
            }
          }
        is TransactionFilter.InputObject ->
          throw Exception("TransactionFilterSerializer: InputObject is not supported")
          //buildJsonObject { put("InputObject", value.objectId.hash) }
        is TransactionFilter.ChangedObject ->
          throw Exception("TransactionFilterSerializer: ChangedObject is not supported")
          //buildJsonObject { put("ChangedObject", value.objectId.hash) }
        is TransactionFilter.FromAddress ->
          buildJsonObject { put("FromAddress", value.address.toString()) }
        is TransactionFilter.ToAddress ->
          buildJsonObject { put("ToAddress", value.address.toString()) }
        is TransactionFilter.FromAndToAddress ->
          buildJsonObject {
            putJsonObject("FromAndToAddress") {
              put("from", value.fromAddress.toString())
              put("to", value.toAddress.toString())
            }
          }
        is TransactionFilter.FromOrToAddress ->
          buildJsonObject { put("FromOrToAddress", value.suiAddress.toString()) }
        else -> {
          throw Exception("TransactionFilterSerializer: Unknown TransactionFilter type")
        }
      }
    encoder.encodeJsonElement(json)
  }

  override fun deserialize(decoder: Decoder): TransactionFilter {
    TODO("Not yet implemented")
  }
}
