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
package xyz.mcxross.ksui.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.core.model.AllowedProposers
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.Epoch
import xyz.mcxross.ksui.core.model.TransactionExpiration

object TransactionExpirationSerializer : KSerializer<TransactionExpiration> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("TransactionExpiration") {
      element("None", buildClassSerialDescriptor("None"))
      element("Epoch", Long.serializer().descriptor)
      element("ValidDuring", buildClassSerialDescriptor("ValidDuring"))
      element("Validity", buildClassSerialDescriptor("Validity"))
    }

  override fun serialize(encoder: Encoder, value: TransactionExpiration) {
    when (value) {
      is TransactionExpiration.None -> encoder.encodeEnum(descriptor, 0)
      is TransactionExpiration.Epoch -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeSerializableValue(Long.serializer(), value.epoch.id.toLong())
      }
      is TransactionExpiration.ValidDuring -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.encodeOptionalLong(value.minEpoch)
        encoder.encodeOptionalLong(value.maxEpoch)
        encoder.encodeOptionalLong(value.minTimestamp)
        encoder.encodeOptionalLong(value.maxTimestamp)
        encoder.encodeSerializableValue(Digest.serializer(), value.chain)
        encoder.encodeInt(value.nonce.toInt())
      }
      is TransactionExpiration.Validity -> {
        encoder.encodeEnum(descriptor, 3)
        encoder.encodeOptionalLong(value.minEpoch)
        encoder.encodeOptionalLong(value.maxEpoch)
        encoder.encodeOptionalLong(value.minTimestamp)
        encoder.encodeOptionalLong(value.maxTimestamp)
        encoder.encodeSerializableValue(Digest.serializer(), value.chain)
        encoder.encodeInt(value.nonce.toInt())
        val proposers = value.allowedProposers
        if (proposers == null) {
          encoder.encodeEnum(descriptor, 0)
        } else {
          encoder.encodeEnum(descriptor, 1)
          encoder.encodeLong(proposers.epoch.toLong())
          encoder.encodeSerializableValue(
            ListSerializer(Int.serializer()),
            proposers.proposers.map { it.toInt() },
          )
        }
      }
    }
  }

  override fun deserialize(decoder: Decoder): TransactionExpiration {
    return when (val index = decoder.decodeEnum(descriptor)) {
      0 -> TransactionExpiration.None
      1 -> {
        val epochVal = decoder.decodeSerializableValue(Long.serializer())
        TransactionExpiration.Epoch(Epoch(epochVal.toString()))
      }
      2 ->
        TransactionExpiration.ValidDuring(
          minEpoch = decoder.decodeOptionalLong(),
          maxEpoch = decoder.decodeOptionalLong(),
          minTimestamp = decoder.decodeOptionalLong(),
          maxTimestamp = decoder.decodeOptionalLong(),
          chain = decoder.decodeSerializableValue(Digest.serializer()),
          nonce = decoder.decodeInt().toUInt(),
        )
      3 ->
        TransactionExpiration.Validity(
          minEpoch = decoder.decodeOptionalLong(),
          maxEpoch = decoder.decodeOptionalLong(),
          minTimestamp = decoder.decodeOptionalLong(),
          maxTimestamp = decoder.decodeOptionalLong(),
          chain = decoder.decodeSerializableValue(Digest.serializer()),
          nonce = decoder.decodeInt().toUInt(),
          allowedProposers =
            when (val option = decoder.decodeEnum(descriptor)) {
              0 -> null
              1 ->
                AllowedProposers(
                  epoch = decoder.decodeLong().toULong(),
                  proposers =
                    decoder.decodeSerializableValue(ListSerializer(Int.serializer())).map {
                      it.toUInt()
                    },
                )
              else ->
                throw SerializationException("Unknown Option<AllowedProposers> index: $option")
            },
        )
      else -> throw SerializationException("Unknown Expiration index: $index")
    }
  }

  private fun Encoder.encodeOptionalLong(value: ULong?) {
    if (value == null) {
      encodeEnum(descriptor, 0)
    } else {
      encodeEnum(descriptor, 1)
      encodeLong(value.toLong())
    }
  }

  private fun Decoder.decodeOptionalLong(): ULong? {
    return when (val index = decodeEnum(descriptor)) {
      0 -> null
      1 -> decodeLong().toULong()
      else -> throw SerializationException("Unknown Option<u64> index: $index")
    }
  }
}
