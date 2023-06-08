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
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import xyz.mcxross.ksui.model.Option

class OptionSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<Option<T>> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
      buildSerialDescriptor("Option", PolymorphicKind.SEALED) {
        element("Some", buildClassSerialDescriptor("Some") { element<String>("message") })
        element("None", dataSerializer.descriptor)
      }

  override fun serialize(encoder: Encoder, value: Option<T>) {
    require(encoder is JsonEncoder)
    val element =
        when (value) {
          is Option.Some -> encoder.json.encodeToJsonElement(dataSerializer, value.value)
          is Option.None -> buildJsonObject { put("status", "ObjectNotFound") }
        }
    encoder.encodeJsonElement(element)
  }

  override fun deserialize(decoder: Decoder): Option<T> {
    require(decoder is JsonDecoder)
    val element = decoder.decodeJsonElement()
    return if (element is JsonObject &&
        (element["status"]?.jsonPrimitive?.content ?: "VersionNotFound") == "VersionFound") {
      Option.Some(decoder.json.decodeFromJsonElement(dataSerializer, element))
    } else {
      Option.None
    }
  }
}
