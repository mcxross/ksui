package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import xyz.mcxross.ksui.model.Command

object CommandSerializer : KSerializer<Command> {

  override val descriptor = buildClassSerialDescriptor("Command")

  override fun serialize(
    encoder: kotlinx.serialization.encoding.Encoder,
    value: Command
  ) {
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
