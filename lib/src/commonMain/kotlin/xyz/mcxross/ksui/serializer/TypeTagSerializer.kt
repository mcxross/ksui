/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import xyz.mcxross.ksui.model.StructTag
import xyz.mcxross.ksui.model.TypeTag

object TypeTagSerializer : KSerializer<TypeTag> {

  override val descriptor: SerialDescriptor by lazy {
    buildClassSerialDescriptor("TypeTag") {
      element("bool", buildClassSerialDescriptor("bool") {}) // Index 0
      element("u8", buildClassSerialDescriptor("u8") {}) // Index 1
      element("u64", buildClassSerialDescriptor("u64") {}) // Index 2
      element("u128", buildClassSerialDescriptor("u128") {}) // Index 3
      element("address", buildClassSerialDescriptor("address") {}) // Index 4
      element("signer", buildClassSerialDescriptor("signer") {}) // Index 5

      element("vector", buildClassSerialDescriptor("vectorPlaceholder") {}) // Index 6
      element("struct", buildClassSerialDescriptor("structPlaceholder") {}) // Index 7

      element("u16", buildClassSerialDescriptor("u16") {}) // Index 8
      element("u32", buildClassSerialDescriptor("u32") {}) // Index 9
      element("u256", buildClassSerialDescriptor("u256") {}) // Index 10
    }
  }

  override fun serialize(encoder: Encoder, value: TypeTag) {
    when (value) {
      is TypeTag.Bool -> encoder.encodeEnum(descriptor, 0)
      is TypeTag.U8 -> encoder.encodeEnum(descriptor, 1)
      is TypeTag.U64 -> encoder.encodeEnum(descriptor, 2)
      is TypeTag.U128 -> encoder.encodeEnum(descriptor, 3)
      is TypeTag.Address -> encoder.encodeEnum(descriptor, 4)
      is TypeTag.Signer -> encoder.encodeEnum(descriptor, 5)
      is TypeTag.Vector -> {
        encoder.encodeEnum(descriptor, 6)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 6, TypeTagSerializer, value.elementType)
          endStructure(descriptor)
        }
      }
      is TypeTag.Struct -> {
        encoder.encodeEnum(descriptor, 7)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 7, StructTag.serializer(), value.tag)
          endStructure(descriptor)
        }
      }
      is TypeTag.U16 -> encoder.encodeEnum(descriptor, 8)
      is TypeTag.U32 -> encoder.encodeEnum(descriptor, 9)
      is TypeTag.U256 -> encoder.encodeEnum(descriptor, 10)
    }
  }

  override fun deserialize(decoder: Decoder): TypeTag {
    return when (val index = decoder.decodeEnum(descriptor)) {
      0 -> TypeTag.Bool
      1 -> TypeTag.U8
      2 -> TypeTag.U64
      3 -> TypeTag.U128
      4 -> TypeTag.Address
      5 -> TypeTag.Signer
      6 -> {
        decoder
          .decodeStructure(descriptor) {
            var typeTag: TypeTag? = null
            if (decodeElementIndex(descriptor) == 6) {
              typeTag = decodeSerializableElement(descriptor, 6, TypeTagSerializer)
            }
            typeTag ?: throw SerializationException("Failed to decode vector TypeTag")
          }
          .let { TypeTag.Vector(it) }
      }
      7 -> {
        decoder
          .decodeStructure(descriptor) {
            var structTag: StructTag? = null
            if (decodeElementIndex(descriptor) == 7) {
              structTag = decodeSerializableElement(descriptor, 7, StructTag.serializer())
            }
            structTag ?: throw SerializationException("Failed to decode StructTag")
          }
          .let { TypeTag.Struct(it) }
      }
      8 -> TypeTag.U16
      9 -> TypeTag.U32
      10 -> TypeTag.U256
      else -> throw SerializationException("Unknown TypeTag index: $index")
    }
  }
}
