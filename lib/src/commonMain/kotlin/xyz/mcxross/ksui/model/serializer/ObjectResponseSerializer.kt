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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.ObjectResponse

class ObjectResponseSerializer : KSerializer<ObjectResponse> {

  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
      buildSerialDescriptor("ObjectResponse", PolymorphicKind.SEALED) {
        element(
            "ObjectData", buildClassSerialDescriptor("ObjectData") { element<String>("message") })
        /*element("ObjectResponseError", serializer().descriptor)*/
      }

  override fun serialize(encoder: Encoder, value: ObjectResponse) {
    TODO("Not yet implemented")
  }

  override fun deserialize(decoder: Decoder): ObjectResponse {
    require(decoder is JsonDecoder)
    val element = decoder.decodeJsonElement()
    if (element is JsonObject && "error" in element) {
      return decoder.json.decodeFromJsonElement<ObjectResponse.ObjectResponseError>(
          serializer(), element.jsonObject["error"]!!)
    }

    val resultElement =
        when (val resultProperty = element.jsonObject["data"]) {
          is JsonObject -> resultProperty.jsonObject
          else -> element
        }

    return decoder.json.decodeFromJsonElement<ObjectResponse.ObjectData>(
        serializer(), resultElement)
  }
}
