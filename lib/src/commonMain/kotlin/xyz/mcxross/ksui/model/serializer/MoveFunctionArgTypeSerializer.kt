package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.model.MoveFunctionArgType

object MoveFunctionArgTypeSerializer : KSerializer<MoveFunctionArgType> {
  override val descriptor = PrimitiveSerialDescriptor("MoveFunctionArgType", PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: MoveFunctionArgType) {
    require(encoder is JsonEncoder)
  }

  override fun deserialize(decoder: Decoder): MoveFunctionArgType {
    require(decoder is JsonDecoder)
    return when (val jsonElement = decoder.decodeJsonElement()) {
      is JsonObject -> {
        MoveFunctionArgType.MoveFunctionArgObject(
          jsonElement["Object"]?.jsonPrimitive?.content ?: "ksui-default"
        )
      }
      is JsonPrimitive -> {
        if (jsonElement.isString) {
          MoveFunctionArgType.MoveFunctionArgString(jsonElement.jsonPrimitive.content)
        } else {
          MoveFunctionArgType.MoveFunctionArgDefault("ksui-default")
        }
      }
      else -> {
        MoveFunctionArgType.MoveFunctionArgDefault("ksui-default")
      }
    }
  }
}
