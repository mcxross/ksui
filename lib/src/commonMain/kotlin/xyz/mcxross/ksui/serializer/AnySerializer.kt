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
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.ptb.Command

object AnySerializer : KSerializer<Any> {
  // Define the descriptor with elements corresponding to the Command variants
  // and a generic placeholder for CallArg/Any.
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("Any") {
      element("MoveCall", Command.MoveCall.serializer().descriptor)
      element("TransferObjects", Command.TransferObjects.serializer().descriptor)
      element("SplitCoins", Command.SplitCoins.serializer().descriptor)
      element("MergeCoins", Command.MergeCoins.serializer().descriptor)
      element("Publish", Command.Publish.serializer().descriptor)
      element("MakeMoveVec", Command.MakeMoveVec.serializer().descriptor)
      element("Upgrade", Command.Upgrade.serializer().descriptor)
      // Placeholder for direct CallArg serialization (Index 7 or generic)
      element("Value", buildClassSerialDescriptor("Value"))
    }

  override fun serialize(encoder: Encoder, value: Any) {
    when (value) {
      is CallArg.Pure -> {
        // CallArg does not use encodeEnum in your logic, strictly structure
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, serializer<CallArg.Pure>(), value)
        }
      }
      is CallArg.Object -> {
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, serializer<CallArg.Object>(), value)
        }
      }
      is Command.MoveCall -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.MoveCall.serializer(), value)
        }
      }
      is Command.TransferObjects -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.TransferObjects.serializer(), value)
        }
      }
      is Command.SplitCoins -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.SplitCoins.serializer(), value)
        }
      }
      is Command.MergeCoins -> {
        encoder.encodeEnum(descriptor, 3)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.MergeCoins.serializer(), value)
        }
      }
      is Command.Publish -> {
        encoder.encodeEnum(descriptor, 4)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.Publish.serializer(), value)
        }
      }
      is Command.MakeMoveVec -> {
        encoder.encodeEnum(descriptor, 5)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.MakeMoveVec.serializer(), value)
        }
      }
      is Command.Upgrade -> {
        encoder.encodeEnum(descriptor, 6)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, Command.Upgrade.serializer(), value)
        }
      }
      else -> throw NotImplementedError("Serializer for ${value::class} is not implemented")
    }
  }

  override fun deserialize(decoder: Decoder): Any {
    // Check if we are decoding a Command (which starts with an Enum index)
    // Note: This assumes the input is a Command. If it's a CallArg,
    // this might misinterpret the first byte if CallArg doesn't use Enum wrapping.
    // However, strictly following the Command structure:

    val index =
      try {
        decoder.decodeEnum(descriptor)
      } catch (e: Exception) {
        // Fallback or rethrow. For BCS, decodeEnum reads a ULEB128.
        throw SerializationException("Failed to read Enum index for AnySerializer", e)
      }

    return decoder.decodeStructure(descriptor) {
      var result: Any? = null

      // We expect the payload to be at index 0 (as written by serialize)
      // even though the Enum index might be different.
      if (decodeElementIndex(descriptor) == 0) {
        result =
          when (index) {
            0 -> decodeSerializableElement(descriptor, 0, Command.MoveCall.serializer())
            1 -> decodeSerializableElement(descriptor, 0, Command.TransferObjects.serializer())
            2 -> decodeSerializableElement(descriptor, 0, Command.SplitCoins.serializer())
            3 -> decodeSerializableElement(descriptor, 0, Command.MergeCoins.serializer())
            4 -> decodeSerializableElement(descriptor, 0, Command.Publish.serializer())
            5 -> decodeSerializableElement(descriptor, 0, Command.MakeMoveVec.serializer())
            6 -> decodeSerializableElement(descriptor, 0, Command.Upgrade.serializer())
            // If index is unknown, it might be a raw CallArg?
            // But we can't easily fallback here once decodeEnum has consumed the byte.
            else -> throw SerializationException("Unknown Command index: $index")
          }
      }
      result ?: throw SerializationException("No data found for index $index")
    }
  }
}
