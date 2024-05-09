package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.TransactionData

object V1Serializer : kotlinx.serialization.KSerializer<TransactionData> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("TransactionData")

  override fun serialize(
    encoder: kotlinx.serialization.encoding.Encoder,
    value: TransactionData
  ) {
    when (value) {
      is TransactionData.V1 -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): TransactionData {
    throw NotImplementedError("Command.SplitCoins is not implemented")
  }
}
