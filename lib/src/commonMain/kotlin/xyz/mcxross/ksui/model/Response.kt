package xyz.mcxross.ksui.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
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

/**
 * A sealed class representing the response of an API call.
 *
 * The response can either be an [Ok] response containing a [Ok.data] of type [T], or an [Error]
 * response
 *
 * containing an HTTP [Error.code] and an error [Error.message].
 */
@Serializable(with = ResponseSerializer::class)
sealed class Response<out T> {

  /** Represents an [Ok] response containing a [data] of type [T]. */
  data class Ok<out T>(val data: T) : Response<T>()
  /** Represents an [Error] response containing an HTTP [code] and an error [message]. */
  data class Error(
    val code: Int,
    val message: String,
  ) : Response<Nothing>()
}

/**
 * A custom serializer for the [Response] class.
 *
 * @param dataSerializer A serializer for the [Response.Ok.data] property of [Response.Ok] objects.
 */
class ResponseSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<Response<T>> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("Response", PolymorphicKind.SEALED) {
      element("Ok", buildClassSerialDescriptor("Ok") { element<String>("message") })
      element("Error", dataSerializer.descriptor)
    }

  override fun serialize(encoder: Encoder, value: Response<T>) {
    require(encoder is JsonEncoder)
    val element =
      when (value) {
        is Response.Ok -> encoder.json.encodeToJsonElement(dataSerializer, value.data)
        is Response.Error ->
          buildJsonObject {
            put(
              "error",
              buildJsonObject {
                put("code", value.code)
                put("message", value.message)
              }
            )
          }
      }
    encoder.encodeJsonElement(element)
  }

  override fun deserialize(decoder: Decoder): Response<T> {
    require(decoder is JsonDecoder)
    val element = decoder.decodeJsonElement()
    if (element is JsonObject && "error" in element) {
      return Response.Error(
        element["error"]!!.jsonObject["code"]!!.jsonPrimitive.int,
        element["error"]!!.jsonObject["message"]!!.jsonPrimitive.content
      )
    }

    val resultElement = when (val resultProperty = element.jsonObject["result"]) {
      is JsonObject -> resultProperty.jsonObject
      is JsonArray -> resultProperty.jsonArray
      else -> element
    }

    return Response.Ok(
      decoder.json.decodeFromJsonElement(
        dataSerializer,
        resultElement
      )
    )
  }
}
