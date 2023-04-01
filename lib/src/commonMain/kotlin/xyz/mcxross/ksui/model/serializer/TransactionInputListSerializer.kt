package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import xyz.mcxross.ksui.model.DataTransactionInput

object TransactionInputListSerializer : KSerializer<List<DataTransactionInput>> {

  override val descriptor: SerialDescriptor =
    ListSerializer(DataTransactionInput.serializer()).descriptor
  override fun serialize(encoder: Encoder, value: List<DataTransactionInput>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      kotlinx.serialization.json.JsonArray(
        value.map { dti ->
          when (dti) {
            is DataTransactionInput.DtiImmOrOwnedObject ->
              kotlinx.serialization.json.buildJsonObject {
                put("type", dti.type)
                put("objectType", dti.objectType)
                put("objectId", dti.objectId)
                put("version", dti.version)
                put("digest", dti.digest)
              }
            is DataTransactionInput.DtiPure ->
              kotlinx.serialization.json.buildJsonObject {
                put("type", dti.type)
                put("valueType", dti.valueType)
                put("value", dti.value)
              }
            is DataTransactionInput.DtiSharedObject ->
              kotlinx.serialization.json.buildJsonObject {
                put("type", dti.type)
                put("objectType", dti.objectType)
                put("objectId", dti.objectId)
                put("initialSharedVersion", dti.initialSharedVersion)
                put("mutable", dti.mutable)
              }
            is DataTransactionInput.DtiDefault ->
              kotlinx.serialization.json.buildJsonObject { put("type", dti.type) }
            else -> {
              kotlinx.serialization.json.buildJsonObject { put("type", "default") }
            }
          }
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<DataTransactionInput> {
    require(decoder is JsonDecoder)
    val jsonArray = decoder.decodeJsonElement().jsonArray
    return jsonArray.map { jsonElement ->
      val jsonObject = jsonElement.jsonObject
      when (val type = jsonObject["objectType"]?.jsonPrimitive?.content) {
        "immOrOwnedObject" ->
          DataTransactionInput.DtiImmOrOwnedObject(
            type = type,
            objectType = jsonObject["objectType"]?.jsonPrimitive?.content!!,
            objectId = jsonObject["objectId"]?.jsonPrimitive?.content!!,
            version = jsonObject["version"]?.jsonPrimitive?.long!!,
            digest = jsonObject["digest"]?.jsonPrimitive?.content!!,
          )
        "sharedObject" ->
          DataTransactionInput.DtiSharedObject(
            type = type,
            objectType = jsonObject["objectType"]?.jsonPrimitive?.content!!,
            objectId = jsonObject["objectId"]?.jsonPrimitive?.content!!,
            initialSharedVersion = jsonObject["initialSharedVersion"]?.jsonPrimitive?.long!!,
            mutable = jsonObject["mutable"]?.jsonPrimitive?.boolean!!,
          )
        else ->
          if ((jsonObject["type"]?.jsonPrimitive?.content ?: "") == "pure") {
            DataTransactionInput.DtiPure(
              type = "pure",
              valueType = jsonObject["valueType"]?.jsonPrimitive?.content!!,
              value = jsonObject["value"]?.jsonPrimitive?.content!!,
            )
          } else {
            DataTransactionInput.DtiDefault(type = "default")
          }
      }
    }
  }
}
