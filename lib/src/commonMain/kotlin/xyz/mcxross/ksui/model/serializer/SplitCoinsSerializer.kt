package xyz.mcxross.ksui.model.serializer

object SplitCoinsSerializer : kotlinx.serialization.KSerializer<xyz.mcxross.ksui.model.Command.SplitCoins> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("SplitCoins")

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: xyz.mcxross.ksui.model.Command.SplitCoins) {
    val composite = encoder.beginStructure(descriptor)
    composite.encodeSerializableElement(descriptor, 0, kotlinx.serialization.serializer(), value)
    composite.endStructure(descriptor)
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): xyz.mcxross.ksui.model.Command.SplitCoins {
    throw NotImplementedError("Command.SplitCoins is not implemented")
  }
}
