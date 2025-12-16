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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.mcxross.ksui.ptb.Argument

object ArgumentSerializer : KSerializer<Argument> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("Argument") {
      element("GasCoin", buildClassSerialDescriptor("GasCoin"))
      element("Input", Argument.Input.serializer().descriptor)
      element("Result", Argument.Result.serializer().descriptor)
      element("NestedResult", Argument.NestedResult.serializer().descriptor)
    }

  override fun serialize(encoder: Encoder, value: Argument) {
    when (value) {
      is Argument.GasCoin -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeStructure(descriptor) {}
      }
      is Argument.Input -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 1, Argument.Input.serializer(), value)
        }
      }
      is Argument.Result -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 2, Argument.Result.serializer(), value)
        }
      }
      is Argument.NestedResult -> {
        encoder.encodeEnum(descriptor, 3)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 3, Argument.NestedResult.serializer(), value)
        }
      }
    }
  }

  override fun deserialize(decoder: Decoder): Argument {
    val index = decoder.decodeEnum(descriptor)
    return decoder.decodeStructure(descriptor) {
      when (index) {
        0 -> {
          Argument.GasCoin
        }
        1 -> {
          decodeSerializableElement(descriptor, 1, Argument.Input.serializer())
        }
        2 -> {
          decodeSerializableElement(descriptor, 2, Argument.Result.serializer())
        }
        3 -> {
          decodeSerializableElement(descriptor, 3, Argument.NestedResult.serializer())
        }
        else -> throw SerializationException("Unknown Argument index: $index")
      }
    }
  }
}
