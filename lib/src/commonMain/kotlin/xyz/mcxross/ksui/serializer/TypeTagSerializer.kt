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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.model.TypeTag

object TypeTagSerializer : KSerializer<TypeTag> {
  override val descriptor =
    buildClassSerialDescriptor("TypeTag") {
      element("data", PrimitiveSerialDescriptor("data", PrimitiveKind.STRING))
    }

  override fun serialize(encoder: Encoder, value: TypeTag) {
    when (value) {
      else -> {
        throw Exception("Unimplemented")
      }
    }
  }

  override fun deserialize(decoder: Decoder): TypeTag {
    return TypeTag.U8
  }
}
