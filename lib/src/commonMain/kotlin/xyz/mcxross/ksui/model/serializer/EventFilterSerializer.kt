package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.Operator

object EventFilterSerializer : KSerializer<EventFilter> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EventFilter") {}
  override fun serialize(encoder: Encoder, value: EventFilter) {
    require(encoder is JsonEncoder)
    val json =
        when (value) {
          is EventFilter.All -> buildJsonObject { putJsonArray("All") {} }
          is EventFilter.Transaction -> buildJsonObject { put("Transaction", value.digest.value) }
          is EventFilter.MoveModule ->
              buildJsonObject {
                putJsonObject("MoveModule") {
                  put("package", value.pakage)
                  put("module", value.module)
                }
              }
          is EventFilter.MoveEvent -> buildJsonObject { put("MoveEvent", value.struct) }
          is EventFilter.Sender -> buildJsonObject { put("Sender", value.address.pubKey) }
          is EventFilter.Package -> buildJsonObject { put("Package", value.id) }
          is EventFilter.TimeRange ->
              buildJsonObject {
                putJsonObject("TimeRange") {
                  put("startTime", value.start.toString())
                  put("endTime", value.end.toString())
                }
              }
          is EventFilter.MoveEventType -> buildJsonObject { put("MoveEventType", value.eventType) }
          is EventFilter.MoveEventField ->
              buildJsonObject {
                putJsonObject("MoveEventField") {
                  put("path", value.dataField.path)
                  put("value", value.dataField.value)
                }
              }
          is EventFilter.Combined ->
              buildJsonObject {
                putJsonArray(
                    when (value.operator) {
                      Operator.AND -> "And"
                      Operator.OR -> "Or"
                      Operator.ALL -> "All"
                      Operator.ANY -> "Any"
                    }) {
                      value.filters.forEach {
                        add(encoder.json.encodeToJsonElement(EventFilter.serializer(), it))
                      }
                    }
              }
          else -> {
            throw Exception("EventFilterSerializer: Unknown EventFilter type")
          }
        }
    encoder.encodeJsonElement(json)
  }

  override fun deserialize(decoder: Decoder): EventFilter {
    TODO("Not yet implemented")
  }
}
