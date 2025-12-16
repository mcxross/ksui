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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.Command
import xyz.mcxross.ksui.ptb.ProgrammableMoveCall

object CommandSerializer : KSerializer<Command> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Command")

  override fun serialize(encoder: Encoder, value: Command) {
    when (value) {
      is Command.MoveCall -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(
            descriptor,
            0,
            ProgrammableMoveCall.serializer(),
            value.moveCall,
          )
        }
      }
      is Command.TransferObjects -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(
            descriptor,
            0,
            ListSerializer(ArgumentSerializer),
            value.objects,
          )
          encodeSerializableElement(descriptor, 1, ArgumentSerializer, value.address)
        }
      }
      is Command.SplitCoins -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, ArgumentSerializer, value.coin)
          encodeSerializableElement(descriptor, 1, ListSerializer(ArgumentSerializer), value.into)
        }
      }
      is Command.MergeCoins -> {
        encoder.encodeEnum(descriptor, 3)
        encoder.encodeStructure(descriptor) {
          encodeSerializableElement(descriptor, 0, ArgumentSerializer, value.coin)
          encodeSerializableElement(descriptor, 1, ListSerializer(ArgumentSerializer), value.coins)
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
    }
  }

  override fun deserialize(decoder: Decoder): Command {
    val index = decoder.decodeEnum(descriptor)
    return decoder.decodeStructure(descriptor) {
      when (index) {
        0 -> { // MoveCall
          var moveCall: ProgrammableMoveCall? = null
          if (decodeElementIndex(descriptor) == 0) {
            moveCall = decodeSerializableElement(descriptor, 0, ProgrammableMoveCall.serializer())
          }
          Command.MoveCall(moveCall ?: throw SerializationException("Missing MoveCall data"))
        }
        1 -> { // TransferObjects
          var objects: List<Argument>? = null
          var address: Argument? = null
          loop@ while (true) {
            when (decodeElementIndex(descriptor)) {
              CompositeDecoder.DECODE_DONE -> break@loop
              0 ->
                objects =
                  decodeSerializableElement(descriptor, 0, ListSerializer(ArgumentSerializer))
              1 -> address = decodeSerializableElement(descriptor, 1, ArgumentSerializer)
            }
          }
          Command.TransferObjects(
            objects ?: throw SerializationException("Missing objects"),
            address ?: throw SerializationException("Missing address"),
          )
        }
        2 -> {
          var coin: Argument? = null
          var into: List<Argument>? = null
          loop@ while (true) {
            when (decodeElementIndex(descriptor)) {
              CompositeDecoder.DECODE_DONE -> break@loop
              0 -> coin = decodeSerializableElement(descriptor, 0, ArgumentSerializer)
              1 ->
                into = decodeSerializableElement(descriptor, 1, ListSerializer(ArgumentSerializer))
            }
          }
          Command.SplitCoins(
            coin ?: throw SerializationException("Missing coin"),
            into ?: throw SerializationException("Missing into"),
          )
        }
        3 -> {
          var coin: Argument? = null
          var coins: List<Argument>? = null
          loop@ while (true) {
            when (decodeElementIndex(descriptor)) {
              CompositeDecoder.DECODE_DONE -> break@loop
              0 -> coin = decodeSerializableElement(descriptor, 0, ArgumentSerializer)
              1 ->
                coins = decodeSerializableElement(descriptor, 1, ListSerializer(ArgumentSerializer))
            }
          }
          Command.MergeCoins(
            coin ?: throw SerializationException("Missing coin"),
            coins ?: throw SerializationException("Missing coins"),
          )
        }
        4 -> {
          var publish: Command.Publish? = null
          if (decodeElementIndex(descriptor) == 0) {
            publish = decodeSerializableElement(descriptor, 0, Command.Publish.serializer())
          }
          publish ?: throw SerializationException("Missing Publish data")
        }
        5 -> {
          var vec: Command.MakeMoveVec? = null
          if (decodeElementIndex(descriptor) == 0) {
            vec = decodeSerializableElement(descriptor, 0, Command.MakeMoveVec.serializer())
          }
          vec ?: throw SerializationException("Missing MakeMoveVec data")
        }
        6 -> {
          var upgrade: Command.Upgrade? = null
          if (decodeElementIndex(descriptor) == 0) {
            upgrade = decodeSerializableElement(descriptor, 0, Command.Upgrade.serializer())
          }
          upgrade ?: throw SerializationException("Missing Upgrade data")
        }
        else -> throw SerializationException("Unknown Command index: $index")
      }
    }
  }
}
