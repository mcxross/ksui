package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.serializer
import xyz.mcxross.ksui.model.TransactionDataV1
import xyz.mcxross.ksui.model.TransactionKind

object TransactionKindSerializer : kotlinx.serialization.KSerializer<TransactionKind> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("TransactionKind")

  override fun serialize(
    encoder: kotlinx.serialization.encoding.Encoder,
    value: TransactionKind
  ) {
    when(value) {
      is TransactionKind.ProgrammableTransaction -> {
        encoder.encodeEnum(descriptor, 0)
        encoder.beginStructure(descriptor).apply {
          encodeSerializableElement(descriptor, 0, serializer(), value)
          endStructure(descriptor)
        }
      }

      else -> {
        throw NotImplementedError("Not yet implemented for tx kind")
      }
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): TransactionKind {
    throw NotImplementedError("Command.SplitCoins is not implemented")
  }
}
