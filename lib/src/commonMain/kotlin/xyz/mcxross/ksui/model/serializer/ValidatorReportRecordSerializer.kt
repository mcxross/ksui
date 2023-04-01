package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.model.ValidatorReportRecord

object ValidatorReportRecordSerializer : KSerializer<List<ValidatorReportRecord>> {
  override val descriptor: SerialDescriptor =
    ListSerializer(ValidatorReportRecord.serializer()).descriptor
  override fun serialize(encoder: Encoder, value: List<ValidatorReportRecord>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      JsonArray(
        value.map { validatorReportRecord ->
          JsonArray(
            listOf(
              JsonPrimitive(validatorReportRecord.hash),
              JsonArray(validatorReportRecord.addresses.map { JsonPrimitive(it) })
            )
          )
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<ValidatorReportRecord> {
    require(decoder is JsonDecoder)
    return decoder.decodeJsonElement().jsonArray.map {
      val jsonArrayElement = it.jsonArray
      ValidatorReportRecord(
        jsonArrayElement[0].jsonPrimitive.content,
        jsonArrayElement[1].jsonArray.map { element -> element.jsonPrimitive.content }
      )
    }
  }
}
