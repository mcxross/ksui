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

import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.model.CallArg

object CallArgPureSerializer : kotlinx.serialization.KSerializer<CallArg.Pure> {

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Pure", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: CallArg.Pure) {
    val composite = encoder.beginStructure(descriptor)
    composite.encodeSerializableElement(
      descriptor,
      0,
      kotlinx.serialization.serializer(),
      value.data,
    )
    composite.endStructure(descriptor)
  }

  override fun deserialize(decoder: Decoder): CallArg.Pure {
    throw NotImplementedError("CallArg.Pure is not implemented")
  }
}
