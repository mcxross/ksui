package xyz.mcxross.ksui.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import xyz.mcxross.ksui.core.model.FundsWithdrawalArg
import xyz.mcxross.ksui.core.model.FundsWithdrawalSource
import xyz.mcxross.ksui.core.model.TypeTag

/** Sui BCS layout: Reservation::MaxAmountU64, WithdrawalTypeArg::Balance, WithdrawFrom. */
object FundsWithdrawalArgSerializer : KSerializer<FundsWithdrawalArg> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("FundsWithdrawalArg") {
      element("reservation", buildClassSerialDescriptor("Reservation"))
      element("typeArg", buildClassSerialDescriptor("WithdrawalTypeArg"))
      element("withdrawFrom", buildClassSerialDescriptor("WithdrawFrom"))
    }

  override fun serialize(encoder: Encoder, value: FundsWithdrawalArg) {
    encoder.encodeEnum(descriptor, 0) // Reservation::MaxAmountU64
    encoder.encodeLong(value.amount.toLong())
    encoder.encodeEnum(descriptor, 0) // WithdrawalTypeArg::Balance
    encoder.encodeSerializableValue(TypeTagSerializer, value.coinType)
    encoder.encodeEnum(descriptor, value.source.ordinal)
  }

  override fun deserialize(decoder: Decoder): FundsWithdrawalArg {
    val reservation = decoder.decodeEnum(descriptor)
    if (reservation != 0) throw SerializationException("Unknown reservation: $reservation")
    val amount = decoder.decodeLong().toULong()
    val typeArg = decoder.decodeEnum(descriptor)
    if (typeArg != 0) throw SerializationException("Unknown withdrawal type: $typeArg")
    val coinType = decoder.decodeSerializableValue(TypeTagSerializer)
    val source = when (val index = decoder.decodeEnum(descriptor)) {
      0 -> FundsWithdrawalSource.Sender
      1 -> FundsWithdrawalSource.Sponsor
      else -> throw SerializationException("Unknown withdrawal source: $index")
    }
    return FundsWithdrawalArg(amount, coinType, source)
  }
}
