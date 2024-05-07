package xyz.mcxross.ksui.model.serializer

object PureSerializer : kotlinx.serialization.KSerializer<xyz.mcxross.ksui.model.CallArg.Pure> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("Pure")

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: xyz.mcxross.ksui.model.CallArg.Pure) {
    encoder.encodeEnum(descriptor, 0)
    encoder.beginStructure(descriptor).apply {
      encodeSerializableElement(descriptor, 0, CallArgPureSerializer, value)
      endStructure(descriptor)
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): xyz.mcxross.ksui.model.CallArg.Pure {
    throw NotImplementedError("BuilderArg.Pure is not implemented")
  }
}
