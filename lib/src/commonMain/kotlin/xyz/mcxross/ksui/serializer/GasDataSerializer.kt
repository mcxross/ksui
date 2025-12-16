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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.GasData
import xyz.mcxross.ksui.model.ObjectReference

object GasDataSerializer : KSerializer<GasData> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("GasData") {
      element("payment", ListSerializer(ObjectReference.serializer()).descriptor)
      element("owner", AccountAddress.serializer().descriptor)
      element("price", Long.serializer().descriptor)
      element("budget", Long.serializer().descriptor)
    }

  override fun serialize(encoder: Encoder, value: GasData) {
    encoder.encodeStructure(descriptor) {
      encodeSerializableElement(
        descriptor,
        0,
        ListSerializer(ObjectReference.serializer()),
        value.payment,
      )
      encodeSerializableElement(descriptor, 1, AccountAddress.serializer(), value.owner)
      encodeSerializableElement(descriptor, 2, Long.serializer(), value.price.toLong())
      encodeSerializableElement(descriptor, 3, Long.serializer(), value.budget.toLong())
    }
  }

  override fun deserialize(decoder: Decoder): GasData {
    return decoder.decodeStructure(descriptor) {
      val payment =
        decodeSerializableElement(descriptor, 0, ListSerializer(ObjectReference.serializer()))
      val owner = decodeSerializableElement(descriptor, 1, AccountAddress.serializer())
      val price = decodeSerializableElement(descriptor, 2, Long.serializer())
      val budget = decodeSerializableElement(descriptor, 3, Long.serializer())

      GasData(payment, owner, price.toULong(), budget.toULong())
    }
  }
}
