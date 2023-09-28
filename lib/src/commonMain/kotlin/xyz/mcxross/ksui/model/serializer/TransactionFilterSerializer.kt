package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import xyz.mcxross.ksui.model.TransactionFilter

object TransactionFilterSerializer : KSerializer<TransactionFilter> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TransactionFilter") {}

  override fun serialize(encoder: Encoder, value: TransactionFilter) {
    require(encoder is JsonEncoder)
    val json =
      when (value) {
        is TransactionFilter.Checkpoint ->
          buildJsonObject { put("Checkpoint", value.checkpointSequenceNumber.toString()) }
        is TransactionFilter.MoveFunction ->
          buildJsonObject {
            putJsonObject("MoveFunction") {
              put("package", value.pakage.hash)
              put("module", value.module)
              put("function", value.function)
            }
          }
        is TransactionFilter.InputObject ->
          buildJsonObject { put("InputObject", value.objectId.hash) }
        is TransactionFilter.ChangedObject ->
          buildJsonObject { put("ChangedObject", value.objectId.hash) }
        is TransactionFilter.FromAddress ->
          buildJsonObject { put("FromAddress", value.address.pubKey) }
        is TransactionFilter.ToAddress -> buildJsonObject { put("ToAddress", value.address.pubKey) }
        is TransactionFilter.FromAndToAddress ->
          buildJsonObject {
            putJsonObject("FromAndToAddress") {
              put("from", value.fromAddress.pubKey)
              put("to", value.toAddress.pubKey)
            }
          }
        is TransactionFilter.FromOrToAddress ->
          buildJsonObject { put("FromOrToAddress", value.suiAddress.pubKey) }
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
