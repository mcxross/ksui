package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import xyz.mcxross.ksui.model.TransactionFilter

object TransactionFilterSerializer : KSerializer<TransactionFilter> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TransactionFilter") {}
  override fun serialize(encoder: Encoder, value: TransactionFilter) {
    require(encoder is JsonEncoder)
    val json =
        when (value) {
          is TransactionFilter.Checkpoint ->
              buildJsonObject { put("Checkpoint", value.checkpointSequenceNumber.toString()) }
          else -> {
            throw Exception("TransactionFilterSerializer: Unknown TransactionFilter type")
          }
        }
    encoder.encodeJsonElement(json)
  }

  override fun deserialize(decoder: Decoder): TransactionFilter {
    TODO("Not yet implemented")
  }
}
