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
package xyz.mcxross.ksui.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.TransactionData

object TransactionDataSerializer : KSerializer<TransactionData> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("TransactionData") {
      element("V1", buildClassSerialDescriptor("V1"))
    }

  override fun serialize(encoder: Encoder, value: TransactionData) {
    when (value) {
      is TransactionData.V1 -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeSerializableValue(TransactionDataV1Serializer, value)
      }
    }
  }

  override fun deserialize(decoder: Decoder): TransactionData {
    return when (val index = decoder.decodeEnum(descriptor)) {
      0 -> decoder.decodeSerializableValue(TransactionDataV1Serializer)
      else -> throw SerializationException("Unknown TransactionData version: $index")
    }
  }
}

private object TransactionDataV1Serializer : KSerializer<TransactionData.V1> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("TransactionDataV1") {
      element("kind", TransactionKindSerializer.descriptor)
      element("sender", AccountAddress.serializer().descriptor)
      element("gasData", GasDataSerializer.descriptor)
      element("expiration", TransactionExpirationSerializer.descriptor)
    }

  override fun serialize(encoder: Encoder, value: TransactionData.V1) {
    encoder.encodeSerializableValue(TransactionKindSerializer, value.kind)
    encoder.encodeSerializableValue(AccountAddress.serializer(), value.sender)
    encoder.encodeSerializableValue(GasDataSerializer, value.gasData)
    encoder.encodeSerializableValue(TransactionExpirationSerializer, value.expiration)
  }

  override fun deserialize(decoder: Decoder): TransactionData.V1 {
    val kind = decoder.decodeSerializableValue(TransactionKindSerializer)
    val sender = decoder.decodeSerializableValue(AccountAddress.serializer())
    val gasData = decoder.decodeSerializableValue(GasDataSerializer)
    val expiration = decoder.decodeSerializableValue(TransactionExpirationSerializer)
    return TransactionData.V1(kind, sender, gasData, expiration)
  }
}
