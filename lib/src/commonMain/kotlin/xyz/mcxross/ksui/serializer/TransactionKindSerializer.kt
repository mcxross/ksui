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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.ptb.TransactionKind

object TransactionKindSerializer : kotlinx.serialization.KSerializer<TransactionKind> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("TransactionKind") {
      element(
        "ProgrammableTransaction",
        TransactionKind.ProgrammableTransaction.serializer().descriptor,
      )
    }

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: TransactionKind) {
    when (value) {
      is TransactionKind.ProgrammableTransaction -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }

      else -> {
        throw NotImplementedError("Not yet implemented for tx kind")
      }
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): TransactionKind {

    val index = decoder.decodeEnum(descriptor)

    return when (index) {
      0 -> {
        decoder.decodeStructure(descriptor) {
          var pt: TransactionKind.ProgrammableTransaction? = null

          loop@ while (true) {
            when (val i = decodeElementIndex(descriptor)) {
              CompositeDecoder.DECODE_DONE -> break@loop
              0 -> {
                pt =
                  decodeSerializableElement(
                    descriptor,
                    0,
                    TransactionKind.ProgrammableTransaction.serializer(),
                  )
              }
              else -> throw SerializationException("Unknown index $i")
            }
          }
          pt ?: throw SerializationException("Failed to decode ProgrammableTransaction")
        }
      }
      else -> {
        throw NotImplementedError("TransactionKind variant index $index is not implemented")
      }
    }
  }
}
