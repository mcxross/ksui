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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.ptb.Command

object AnySerializer : kotlinx.serialization.KSerializer<Any> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("Any")

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Any) {
    when (value) {
      is CallArg.Pure -> {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.MoveCall -> {
        encoder.encodeEnum(descriptor, 0)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.TransferObjects -> {
        encoder.encodeEnum(descriptor, 1)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.SplitCoins -> {
        encoder.encodeEnum(descriptor, 2)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.MergeCoins -> {
        encoder.encodeEnum(descriptor, 3)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.Publish -> {
        encoder.encodeEnum(descriptor, 4)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.MakeMoveVec -> {
        encoder.encodeEnum(descriptor, 5)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      is Command.Upgrade -> {
        encoder.encodeEnum(descriptor, 6)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          kotlinx.serialization.serializer(),
          value,
        )
        composite.endStructure(descriptor)
      }
      else -> throw NotImplementedError("Any is not implemented")
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Any {
    return decoder.decodeStructure(descriptor) {
      var result: Any? = null
      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          CompositeDecoder.DECODE_DONE -> break
          0 -> result = decodeSerializableElement(descriptor, 0, CallArg.Pure.serializer())
          else -> throw SerializationException("Unexpected index $index")
        }
      }
      result ?: throw SerializationException("No data found")
    }
  }
}
