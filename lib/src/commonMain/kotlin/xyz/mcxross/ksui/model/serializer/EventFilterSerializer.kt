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
import xyz.mcxross.ksui.model.EventFilter

object EventFilterSerializer : KSerializer<EventFilter> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EventFilter") {}
  override fun serialize(encoder: Encoder, value: EventFilter) {
    require(encoder is JsonEncoder)
    val json =
        when (value) {
          is EventFilter.All -> buildJsonObject { putJsonObject("All") {} }
          is EventFilter.MoveModule ->
              buildJsonObject {
                putJsonObject("MoveModule") {
                  put("package", value.pakage)
                  put("module", value.module)
                }
              }
          is EventFilter.TimeRange ->
              buildJsonObject {
                putJsonObject("TimeRange") {
                  put("startTime", value.start.toString())
                  put("endTime", value.end.toString())
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
