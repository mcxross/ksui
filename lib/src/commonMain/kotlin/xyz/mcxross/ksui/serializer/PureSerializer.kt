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

object PureSerializer : kotlinx.serialization.KSerializer<xyz.mcxross.ksui.model.CallArg.Pure> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("Pure")

  override fun serialize(
    encoder: kotlinx.serialization.encoding.Encoder,
    value: xyz.mcxross.ksui.model.CallArg.Pure,
  ) {
    encoder.encodeEnum(descriptor, 0)
    encoder.beginStructure(descriptor).apply {
      encodeSerializableElement(descriptor, 0, CallArgPureSerializer, value)
      endStructure(descriptor)
    }
  }

  override fun deserialize(
    decoder: kotlinx.serialization.encoding.Decoder
  ): xyz.mcxross.ksui.model.CallArg.Pure {
    throw NotImplementedError("BuilderArg.Pure is not implemented")
  }
}
