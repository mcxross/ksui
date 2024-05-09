package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SuiAddressSerializer : KSerializer<ByteArray> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SuiAddress") {
    element("data", PrimitiveSerialDescriptor("data", PrimitiveKind.BYTE))
  }

  override fun serialize(encoder: Encoder, value: ByteArray) {
    encoder.beginStructure(descriptor).apply {
      value.forEachIndexed { index, byte ->
        encodeByteElement(descriptor, index, byte)
      }
      endStructure(descriptor)
    }
  }

  override fun deserialize(decoder: Decoder): ByteArray {
    TODO()
  }
}
