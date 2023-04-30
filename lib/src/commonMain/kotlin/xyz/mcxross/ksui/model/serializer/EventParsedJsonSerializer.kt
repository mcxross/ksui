package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import xyz.mcxross.ksui.model.EventParsedJson

object EventParsedJsonSerializer : KSerializer<EventParsedJson> {
  override val descriptor = PrimitiveSerialDescriptor("EventParsedJson", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: EventParsedJson) {}

  override fun deserialize(decoder: Decoder): EventParsedJson {
    require(decoder is JsonDecoder)
    val jsonElement = decoder.decodeJsonElement()
    return EventParsedJson(jsonElement.toString())
  }
}
