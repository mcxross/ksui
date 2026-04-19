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
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.mcxross.ksui.model.CallArg

object CallArgSerializer : KSerializer<CallArg> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("CallArg") {
      element("Pure", ByteArraySerializer().descriptor)
      element("Object", ObjectArgSerializer.descriptor)
    }

  override fun serialize(encoder: Encoder, value: CallArg) {
    when (value) {
      is CallArg.Pure -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, ByteArraySerializer(), value.data)
        }
      }
      is CallArg.Object -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 1, ObjectArgSerializer, value.arg)
        }
      }

      else -> {}
    }
  }

  override fun deserialize(decoder: Decoder): CallArg {
    val index = decoder.decodeEnum(descriptor)
    return decoder.decodeStructure(descriptor) {
      when (index) {
        0 -> {
          val data = decodeSerializableElement(descriptor, 0, ByteArraySerializer())
          CallArg.Pure(data)
        }
        1 -> {
          val arg = decodeSerializableElement(descriptor, 1, ObjectArgSerializer)
          CallArg.Object(arg)
        }
        else -> throw SerializationException("Unknown CallArg index: $index")
      }
    }
  }
}
