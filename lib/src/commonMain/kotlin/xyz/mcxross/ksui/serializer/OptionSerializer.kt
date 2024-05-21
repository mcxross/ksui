/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.serializer

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
    return if (
      element is JsonObject &&
        (element["status"]?.jsonPrimitive?.content ?: "VersionNotFound") == "VersionFound"
    ) {
      Option.Some(decoder.json.decodeFromJsonElement(dataSerializer, element))
    } else {
      Option.None
    }
  }
}
