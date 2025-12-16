package xyz.mcxross.ksui.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import xyz.mcxross.ksui.ptb.Command
import xyz.mcxross.ksui.ptb.ProgrammableMoveCall

object CommandSerializer : KSerializer<Command> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("Command") {
      element("MoveCall", ProgrammableMoveCall.serializer().descriptor)
      element("TransferObjects", buildClassSerialDescriptor("TransferObjects"))
      element("SplitCoins", buildClassSerialDescriptor("SplitCoins"))
      element("MergeCoins", buildClassSerialDescriptor("MergeCoins"))
      element("Publish", Command.Publish.serializer().descriptor)
      element("MakeMoveVec", Command.MakeMoveVec.serializer().descriptor)
      element("Upgrade", Command.Upgrade.serializer().descriptor)
    }

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
        0 -> {
          val moveCall = decodeSerializableElement(descriptor, 0, ProgrammableMoveCall.serializer())
          Command.MoveCall(moveCall)
        }
        1 -> {
          val objects = decodeSerializableElement(descriptor, 0, ListSerializer(ArgumentSerializer))
          val address = decodeSerializableElement(descriptor, 1, ArgumentSerializer)
          Command.TransferObjects(objects, address)
        }
        2 -> {
          val coin = decodeSerializableElement(descriptor, 0, ArgumentSerializer)
          val into = decodeSerializableElement(descriptor, 1, ListSerializer(ArgumentSerializer))
          Command.SplitCoins(coin, into)
        }
        3 -> {
          val coin = decodeSerializableElement(descriptor, 0, ArgumentSerializer)
          val coins = decodeSerializableElement(descriptor, 1, ListSerializer(ArgumentSerializer))
          Command.MergeCoins(coin, coins)
        }
        4 -> {
          val publish = decodeSerializableElement(descriptor, 0, Command.Publish.serializer())
          Command.Publish(publish.bytes, publish.dependencies)
        }
        5 -> {
          val vec = decodeSerializableElement(descriptor, 0, Command.MakeMoveVec.serializer())
          Command.MakeMoveVec(vec.typeTag, vec.values)
        }
        6 -> {
          val upgrade = decodeSerializableElement(descriptor, 0, Command.Upgrade.serializer())
          Command.Upgrade(
            upgrade.modules,
            upgrade.dependencies,
            upgrade.packageId,
            upgrade.upgradeTicket,
          )
        }
        else -> throw SerializationException("Unknown Command index: $index")
      }
    }
  }
}
