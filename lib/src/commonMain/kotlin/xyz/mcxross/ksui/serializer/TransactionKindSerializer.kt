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

import kotlinx.serialization.serializer
import xyz.mcxross.ksui.ptb.TransactionKind

object TransactionKindSerializer : kotlinx.serialization.KSerializer<TransactionKind> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("TransactionKind")

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
    throw NotImplementedError("Command.SplitCoins is not implemented")
  }
}