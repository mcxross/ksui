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
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.TransactionKind

object TransactionKindSerializer : KSerializer<TransactionKind> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("TransactionKind") {
      element("ProgrammableTransaction", ProgrammableTransaction.serializer().descriptor)
    }

  override fun serialize(encoder: Encoder, value: TransactionKind) {
    when (value) {
      is TransactionKind.ProgrammableTransaction -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeSerializableValue(ProgrammableTransaction.serializer(), value.pt)
      }
      else -> throw SerializationException("Unsupported TransactionKind: $value")
    }
  }

  override fun deserialize(decoder: Decoder): TransactionKind {
    return when (val index = decoder.decodeEnum(descriptor)) {
      0 -> {
        val pt = decoder.decodeSerializableValue(ProgrammableTransaction.serializer())
        TransactionKind.ProgrammableTransaction(pt)
      }
      else -> throw SerializationException("Unknown TransactionKind index: $index")
    }
  }
}
