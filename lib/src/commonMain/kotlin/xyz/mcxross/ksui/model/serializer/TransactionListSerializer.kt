package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.model.TransactionKind

object TransactionListSerializer : KSerializer<List<TransactionKind>> {
  override val descriptor: SerialDescriptor =
    ListSerializer(TransactionKind.serializer()).descriptor
  override fun serialize(encoder: Encoder, value: List<TransactionKind>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      kotlinx.serialization.json.JsonArray(
        value.map { tk ->
          when (tk) {
            is TransactionKind.MoveCall -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.Publish -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.PaySui -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.PayAllSui -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.Transfer -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.TransferSui -> kotlinx.serialization.json.buildJsonObject {}
            is TransactionKind.SplitCoin -> kotlinx.serialization.json.buildJsonObject {}
            else -> {
              kotlinx.serialization.json.buildJsonObject {}
            }
          }
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<TransactionKind> {
    require(decoder is JsonDecoder)
    val jsonArray = decoder.decodeJsonElement().jsonArray
    return jsonArray.map { jsonElement ->
      val jsonObject = jsonElement.jsonObject
      if (jsonObject.containsKey("MoveCall")) {
        val moveCallObject = jsonObject["MoveCall"]?.jsonObject
        TransactionKind.MoveCall(
          moveCallObject?.get("package")?.jsonPrimitive?.content ?: "ksui-default",
          moveCallObject?.get("module")?.jsonPrimitive?.content ?: "ksui-default",
          moveCallObject?.get("function")?.jsonPrimitive?.content ?: "ksui-default",
          moveCallObject?.get("type_arguments")?.jsonArray?.map { it.jsonPrimitive.content },
        )
      } else if (jsonObject.containsKey("SplitCoins")) {
        val splitCoinsObject = jsonObject["SplitCoins"]?.jsonArray
        TransactionKind.SplitCoin(emptyList())
      } else {
        TransactionKind.DefaultTransaction("default")
      }
    }
  }
}
