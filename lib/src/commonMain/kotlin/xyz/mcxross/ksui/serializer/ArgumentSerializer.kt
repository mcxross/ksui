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

import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.ptb.Argument

object ArgumentSerializer : kotlinx.serialization.KSerializer<Argument> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    buildClassSerialDescriptor("GasCoin")

  override fun serialize(encoder: Encoder, value: Argument) {
    when (value) {
      is Argument.GasCoin -> {
        encoder.encodeEnum(descriptor, 0)
      }
      is Argument.Input -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }
      is Argument.Result -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }
      is Argument.NestedResult -> {
        encoder.encodeEnum(descriptor, 3)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }
    }
  }

  override fun deserialize(decoder: Decoder): Argument {
    return Argument.GasCoin
  }
}
