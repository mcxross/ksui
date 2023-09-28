package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.EventEnvelope
import xyz.mcxross.ksui.model.TransactionSubscriptionResponse

object TxnSubResSerializer : KSerializer<TransactionSubscriptionResponse> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("TransactionSubscriptionResponse", PolymorphicKind.SEALED) {
      element("Ok", buildClassSerialDescriptor("Ok") { element<Long>("subscriptionId") })
      element("Effect", buildClassSerialDescriptor("Effect") { element<EventEnvelope>("effect") })
      element(
        "Error",
        buildClassSerialDescriptor("Error") {
          element<Int>("code")
          element<String>("message")
        }
      )
    }

  override fun serialize(encoder: Encoder, value: TransactionSubscriptionResponse) {
    TODO("Not yet implemented")
  }

  override fun deserialize(decoder: Decoder): TransactionSubscriptionResponse {
    require(decoder is JsonDecoder)
    val element = decoder.decodeJsonElement()
    if (element is JsonObject && "error" in element) {
      return TransactionSubscriptionResponse.Error(
        element["error"]!!.jsonObject["code"]!!.jsonPrimitive.int,
        element["error"]!!.jsonObject["message"]!!.jsonPrimitive.content
      )
    }

    if (element is JsonPrimitive) {
      return TransactionSubscriptionResponse.Ok(element.jsonPrimitive.long)
    }

    return TransactionSubscriptionResponse.Effect(
      decoder.json.decodeFromJsonElement(
        serializer(),
        element.jsonObject["params"]!!.jsonObject["result"]!!
      )
    )
  }
}
