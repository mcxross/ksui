package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.Argument

object ArgumentSerializer : kotlinx.serialization.KSerializer<Argument> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    buildClassSerialDescriptor("GasCoin")

  override fun serialize(encoder: Encoder, value: Argument) {
    when (value) {
      is Argument.GasCoin -> {
        encoder.encodeEnum(descriptor, 0)
      }

      is Argument.Input -> {
        encoder.encodeEnum(descriptor, 1)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }

      is Argument.Result -> {
        encoder.encodeEnum(descriptor, 2)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }

      is Argument.NestedResult -> {
        encoder.encodeEnum(descriptor, 3)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }
    }
  }

  override fun deserialize(decoder: Decoder): Argument {
    return Argument.GasCoin
  }
}
