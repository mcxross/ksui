package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.model.Validator

object SuiCommitteeValidatorSerializer : KSerializer<List<Validator>> {
  override val descriptor: SerialDescriptor = ListSerializer(Validator.serializer()).descriptor

  override fun serialize(encoder: Encoder, value: List<Validator>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      JsonArray(
        value.map { validator ->
          JsonArray(
            listOf(
              buildJsonArray {
                add(validator.publicKey)
                add(validator.weight)
              },
            )
          )
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<Validator> {
    require(decoder is JsonDecoder)
    val jsonArray = decoder.decodeJsonElement().jsonArray
    return jsonArray.map { jsonElement ->
      if (jsonElement is JsonArray) {
        Validator(
          publicKey = jsonElement[0].jsonPrimitive.content,
          weight = jsonElement[1].jsonPrimitive.int
        )
      } else {
        throw IllegalArgumentException("Invalid validator format")
      }
    }
  }
}
