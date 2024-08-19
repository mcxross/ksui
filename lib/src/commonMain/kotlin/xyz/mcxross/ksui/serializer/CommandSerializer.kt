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
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import xyz.mcxross.ksui.ptb.Command

object CommandSerializer : KSerializer<Command> {

  override val descriptor = buildClassSerialDescriptor("Command")

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Command) {
    when (value) {
      is Command.SplitCoins -> {
        encoder.encodeEnum(descriptor, 9)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, kotlinx.serialization.serializer(), value)
          endStructure(descriptor)
        }
      }
      else -> {
        throw NotImplementedError("Command is not implemented")
      }
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Command {
    return TODO()
  }
}
