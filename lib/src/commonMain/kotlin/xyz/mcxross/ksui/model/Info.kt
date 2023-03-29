package xyz.mcxross.ksui.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class Validator(val publicKey: String, val weight: Int)

@Serializable
data class CommitteeInfo(
  val epoch: Long,
  @Polymorphic
  val validators: List<Validator>,
)

object ValidatorSerializer : KSerializer<Validator> {

  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Validator") {
    element("publicKey", String.serializer().descriptor)
    element("weight", Int.serializer().descriptor)
  }

  override fun serialize(encoder: Encoder, value: Validator) {
    val composite = encoder.beginStructure(descriptor)
    composite.encodeStringElement(descriptor, 0, value.publicKey)
    composite.encodeIntElement(descriptor, 1, value.weight)
    composite.endStructure(descriptor)
  }

  override fun deserialize(decoder: Decoder): Validator {
    val composite = decoder.beginStructure(descriptor)
    var publicKey = ""
    var weight = 0
    loop@ while (true) {
      when (val index = composite.decodeElementIndex(descriptor)) {
        CompositeDecoder.DECODE_DONE -> break@loop
        0 -> publicKey = composite.decodeStringElement(descriptor, index)
        1 -> weight = composite.decodeIntElement(descriptor, index)
        else -> throw SerializationException("Unknown index $index")
      }
    }
    composite.endStructure(descriptor)
    return Validator(publicKey, weight)
  }
}

@Serializable data class SuiCommittee(
  val epoch: Long,
  @Polymorphic
  val validators: List<Validator>,
)
