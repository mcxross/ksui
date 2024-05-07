package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.model.Command

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
