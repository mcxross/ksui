package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonObject

object ToStringSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("ToStringSerializer", PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: String) {
    require(encoder is JsonEncoder)
    val jsonElement = parseToJsonElement(value)
    encoder.encodeJsonElement(jsonElement)
  }

  override fun deserialize(decoder: Decoder): String {
    require(decoder is JsonDecoder)
    val jsonElement = decoder.decodeJsonElement()
    return jsonElement.jsonObject.toString()
  }
}
