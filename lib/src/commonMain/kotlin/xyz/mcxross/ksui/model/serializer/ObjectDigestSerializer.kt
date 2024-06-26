package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectDigest

object ObjectDigestSerializer : KSerializer<ObjectDigest> {
  override val descriptor: SerialDescriptor =  buildClassSerialDescriptor("ObjectDigest")

  override fun serialize(encoder: Encoder, value: ObjectDigest) {
    encoder.beginStructure(descriptor).apply {
      encodeSerializableElement(descriptor, 0, Digest.serializer(), value.digest)
      endStructure(descriptor)
    }
  }

  override fun deserialize(decoder: Decoder): ObjectDigest {
    return ObjectDigest(Digest(decoder.decodeString()))
  }
}
