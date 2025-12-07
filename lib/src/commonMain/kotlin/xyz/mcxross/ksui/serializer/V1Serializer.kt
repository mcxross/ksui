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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.B
import xyz.mcxross.ksui.model.GasData
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.TransactionData
import xyz.mcxross.ksui.model.TransactionDataV1
import xyz.mcxross.ksui.model.TransactionExpiration
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.TransactionKind

object V1Serializer : KSerializer<TransactionData> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor(" xyz.mcxross.ksui.model.TransactionData") {
      element("v1", ProgrammableTransaction.serializer().descriptor)
      element("sender", String.serializer().descriptor)
      element("gasPayment", ListSerializer(ObjectReference.serializer()).descriptor)
      element("gasBudget", Long.serializer().descriptor)
      element("gasPrice", Long.serializer().descriptor)
    }

  override fun serialize(encoder: Encoder, value: TransactionData) {
    when (value) {
      is TransactionData.V1 -> {
        // encoder.encodeEnum(TransactionData.V1.serializer().descriptor)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }
    }
  }

  override fun deserialize(decoder: Decoder): TransactionData {
    return decoder.decodeStructure(descriptor) {
      var a = B.A
      var kind: TransactionKind = TransactionKind.DefaultTransaction("DefaultTransaction")
      var sender = ""
      var gasData = GasData(emptyList(), AccountAddress.EMPTY, 0u, 0u)
      var expiration: TransactionExpiration = TransactionExpiration.None

      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> a = decodeSerializableElement(descriptor, 0, serializer())
          1 -> kind = decodeSerializableElement(descriptor, 1, TransactionKind.serializer())
          2 -> sender = decodeStringElement(descriptor, 2)
          3 -> gasData = decodeSerializableElement(descriptor, 3, GasData.serializer())
          4 ->
            expiration =
              decodeSerializableElement(descriptor, 4, TransactionExpiration.serializer())
          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }

      TransactionData.V1(
        TransactionDataV1(
          a = a,
          kind = kind,
          sender = AccountAddress.fromString(sender),
          gasData = gasData,
          expiration = expiration,
        )
      )
    }
  }
}
