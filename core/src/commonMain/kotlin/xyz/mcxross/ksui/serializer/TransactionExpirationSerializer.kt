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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.model.TransactionExpiration

object TransactionExpirationSerializer : KSerializer<TransactionExpiration> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("TransactionExpiration") {
      element("None", buildClassSerialDescriptor("None"))
      element("Epoch", Long.serializer().descriptor)
    }

  override fun serialize(encoder: Encoder, value: TransactionExpiration) {
    when (value) {
      is TransactionExpiration.None -> encoder.encodeEnum(descriptor, 0)
      is TransactionExpiration.Epoch -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeSerializableValue(Long.serializer(), value.epoch.id.toLong())
      }
    }
  }

  override fun deserialize(decoder: Decoder): TransactionExpiration {
    return when (val index = decoder.decodeEnum(descriptor)) {
      0 -> TransactionExpiration.None
      1 -> {
        val epochVal = decoder.decodeSerializableValue(Long.serializer())
        TransactionExpiration.Epoch(xyz.mcxross.ksui.model.Epoch(epochVal.toString()))
      }
      else -> throw SerializationException("Unknown Expiration index: $index")
    }
  }
}
