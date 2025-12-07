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
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.bcs.internal.BcsDecoder
import xyz.mcxross.ksui.model.AccountAddress

object AccountAddressSerializer : KSerializer<AccountAddress> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("AccountAddress") {
      element("data", PrimitiveSerialDescriptor("data", PrimitiveKind.BYTE))
    }

  override fun serialize(encoder: Encoder, value: AccountAddress) {
    encoder.beginStructure(descriptor).apply {
      value.data.forEachIndexed { index, byte -> encodeByteElement(descriptor, index, byte) }
      endStructure(descriptor)
    }
  }

  override fun deserialize(decoder: Decoder): AccountAddress {
    decoder as BcsDecoder
    val bytes = ByteArray(AccountAddress.LENGTH)
    for (i in 0 until AccountAddress.LENGTH) {
      bytes[i] = decoder.decodeByte()
    }
    return AccountAddress(bytes)
  }
}
