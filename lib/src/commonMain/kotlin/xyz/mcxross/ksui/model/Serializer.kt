package xyz.mcxross.ksui.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object DisassembledFieldSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EventField", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Any) {
    encoder.encodeString(Json.encodeToString(value))
  }

  override fun deserialize(decoder: Decoder): Any {
    return decoder.decodeString()
  }
}
