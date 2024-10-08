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
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.model.ObjectArg

object CallArgObjectSerializer : KSerializer<CallArg.Object> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("CallArg.Object") {}

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: CallArg.Object) {
    encoder.encodeEnum(descriptor, 1)
    when (value.arg) {
      is ObjectArg.ImmOrOwnedObject -> {
        encoder.encodeEnum(descriptor, 0)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          ObjectArg.ImmOrOwnedObject.serializer(),
          value.arg,
        )
      }
      is ObjectArg.SharedObject -> {
        encoder.encodeEnum(descriptor, 1)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          ObjectArg.SharedObject.serializer(),
          value.arg,
        )
      }

      is ObjectArg.Receiving -> {
        encoder.encodeEnum(descriptor, 2)
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(
          descriptor,
          0,
          ObjectArg.Receiving.serializer(),
          value.arg,
        )
      }
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): CallArg.Object {
    throw NotImplementedError("CallArg.Object is not implemented")
  }
}
