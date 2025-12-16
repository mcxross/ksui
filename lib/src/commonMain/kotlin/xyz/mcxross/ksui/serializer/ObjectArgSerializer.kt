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
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.mcxross.ksui.model.ObjectArg

object ObjectArgSerializer : KSerializer<ObjectArg> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ObjectArg")

  override fun serialize(encoder: Encoder, value: ObjectArg) {
    when (value) {
      is ObjectArg.ImmOrOwnedObject -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, ObjectArg.ImmOrOwnedObject.serializer(), value)
        }
      }
      is ObjectArg.SharedObject -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, ObjectArg.SharedObject.serializer(), value)
        }
      }
      is ObjectArg.Receiving -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, ObjectArg.Receiving.serializer(), value)
        }
      }
    }
  }

  override fun deserialize(decoder: Decoder): ObjectArg {
    val index = decoder.decodeEnum(descriptor)
    return decoder.decodeStructure(descriptor) {
      var result: ObjectArg? = null
      while (true) {
        when (val i = decodeElementIndex(descriptor)) {
          CompositeDecoder.DECODE_DONE -> break
          0 -> {
            result =
              when (index) {
                0 ->
                  decodeSerializableElement(descriptor, 0, ObjectArg.ImmOrOwnedObject.serializer())
                1 -> decodeSerializableElement(descriptor, 0, ObjectArg.SharedObject.serializer())
                2 -> decodeSerializableElement(descriptor, 0, ObjectArg.Receiving.serializer())
                else -> throw SerializationException("Unknown ObjectArg index: $index")
              }
          }
          else -> throw SerializationException("Unexpected index $i for ObjectArg")
        }
      }
      result ?: throw SerializationException("Failed to decode ObjectArg content")
    }
  }
}
