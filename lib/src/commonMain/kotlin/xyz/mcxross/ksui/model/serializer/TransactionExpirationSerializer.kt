package xyz.mcxross.ksui.model.serializer

import xyz.mcxross.ksui.model.TransactionExpiration

object TransactionExpirationSerializer : kotlinx.serialization.KSerializer<TransactionExpiration> {
  override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
    kotlinx.serialization.descriptors.buildClassSerialDescriptor("TransactionExpiration")

  override fun serialize(
    encoder: kotlinx.serialization.encoding.Encoder,
    value: TransactionExpiration
  ) {
    when(value) {
      is TransactionExpiration.None -> {
        encoder.encodeEnum(descriptor, 0)
      }

      else -> {
        throw NotImplementedError("Not yet implemented for tx kind")
      }
    }
  }

  override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): TransactionExpiration {
    throw NotImplementedError("Command.SplitCoins is not implemented")
  }
}
