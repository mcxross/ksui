package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.model.CallArg

object CallArgPureSerializer : kotlinx.serialization.KSerializer<CallArg.Pure> {

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Pure", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: CallArg.Pure) {
    val composite = encoder.beginStructure(descriptor)
    composite.encodeSerializableElement(descriptor, 0, kotlinx.serialization.serializer(), value.data)
    composite.endStructure(descriptor)
}

  override fun deserialize(decoder: Decoder): CallArg.Pure {
    throw NotImplementedError("CallArg.Pure is not implemented")
  }
}
