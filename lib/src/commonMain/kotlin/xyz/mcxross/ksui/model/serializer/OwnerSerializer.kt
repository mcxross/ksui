package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import xyz.mcxross.ksui.model.Owner
import xyz.mcxross.ksui.model.serializer.util.whichOwner

object OwnerSerializer : KSerializer<Owner> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("Owner", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Owner) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Owner {
    require(decoder is JsonDecoder)
    return when (val json = decoder.decodeJsonElement()) {
      is JsonObject -> {
        whichOwner(decoder.decodeJsonElement().jsonObject)
      }
      is JsonPrimitive -> {
        Owner.LiteralOwner(json.content)
      }
      else -> {
        Owner.AddressOwner("default")
      }
    }
  }
}
