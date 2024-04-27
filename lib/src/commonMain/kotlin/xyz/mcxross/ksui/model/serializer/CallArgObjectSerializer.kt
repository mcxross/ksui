package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import xyz.mcxross.ksui.model.CallArg

object CallArgObjectSerializer : KSerializer<CallArg.Object> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("CallArg.Object") {}

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: CallArg.Object) {
    encoder.encodeByte(1)
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): CallArg.Object {
    throw NotImplementedError("CallArg.Object is not implemented")
  }
}
