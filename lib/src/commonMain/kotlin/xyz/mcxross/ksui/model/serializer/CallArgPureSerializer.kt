package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection
import xyz.mcxross.ksui.model.CallArg

object CallArgPureSerializer : kotlinx.serialization.KSerializer<CallArg.Pure> {
  override val descriptor: SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("CallArg.Pure") {
      element("data", kotlinx.serialization.serializer<Byte>().descriptor)
    }

  override fun serialize(encoder: Encoder, value: CallArg.Pure) {
    encoder.encodeByte(0)
    encoder.encodeCollection(descriptor, value.data.size) {
      for (element in value.data) {
        encoder.encodeByte(element)
      }
    }
  }

  override fun deserialize(decoder: Decoder): CallArg.Pure {
    throw NotImplementedError("CallArg.Pure is not implemented")
  }
}
