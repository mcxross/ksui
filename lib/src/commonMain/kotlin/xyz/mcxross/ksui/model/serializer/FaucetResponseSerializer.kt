package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import xyz.mcxross.ksui.model.FaucetResponse
import xyz.mcxross.ksui.model.TransferredGasObject

object FaucetResponseSerializer : KSerializer<FaucetResponse> {
  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("FaucetResponse", PolymorphicKind.SEALED) {
      element("Ok", buildClassSerialDescriptor("Ok"))
      element("Error", buildClassSerialDescriptor("Error"))
    }

  // TODO: Not tested
  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: FaucetResponse) {
    require(encoder is JsonEncoder)

    val json = buildJsonObject {
      if (value is FaucetResponse.Ok) {
        put(
          "transferredGasObjects",
          JsonArray(
            value.transferredGasObjects.map {
              buildJsonObject {
                put("amount", it.amount)
                put("id", it.id)
                put("transferTxDigest", it.transferTxDigest)
              }
            }
          )
        )
        put("error", null)
      } else if (value is FaucetResponse.Error) {
        put("transferredGasObjects", null)
        put(
          "error",
          buildJsonObject {
            put("code", value.code)
            put("message", value.message)
          }
        )
      }
    }

    encoder.encodeJsonElement(json)
  }

  override fun deserialize(decoder: Decoder): FaucetResponse {
    require(decoder is JsonDecoder)
    val json = decoder.decodeJsonElement()
    if (json is JsonObject) {
      return if (json["transferredGasObjects"]?.jsonArray?.isEmpty() == true) {
        FaucetResponse.Error(
          json["error"]?.jsonObject?.get("code")?.jsonPrimitive?.int ?: 0,
          json["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content ?: "",
        )
      } else {
        FaucetResponse.Ok(
          json["transferredGasObjects"]?.jsonArray?.map {
            TransferredGasObject(
              it.jsonObject["amount"]?.jsonPrimitive?.int ?: 0,
              it.jsonObject["id"]?.jsonPrimitive?.content ?: "",
              it.jsonObject["transferTxDigest"]?.jsonPrimitive?.content ?: "",
            )
          } ?: emptyList()
        )
      }
    } else {
      throw Exception("Unknown error. Maybe the response is not a json object.")
    }
  }
}
