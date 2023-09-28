package xyz.mcxross.ksui.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import xyz.mcxross.ksui.model.ObjectChange
import xyz.mcxross.ksui.model.Owner
import xyz.mcxross.ksui.model.serializer.util.whichOwner

object ObjectChangeSerializer : KSerializer<List<ObjectChange>> {
  override val descriptor: SerialDescriptor = ListSerializer(ObjectChange.serializer()).descriptor

  override fun serialize(encoder: Encoder, value: List<ObjectChange>) {
    require(encoder is JsonEncoder)
    val jsonArray =
      JsonArray(
        value.map { oc ->
          buildJsonObject {
            put("type", oc.type)
            put("sender", oc.sender)
            when (oc.owner) {
              is Owner.SharedOwner -> {
                putJsonObject("owner") {
                  putJsonObject("Shared") {
                    put(
                      "initial_shared_version",
                      (oc.owner as Owner.SharedOwner).shared.initialSharedVersion
                    )
                  }
                }
              }
              is Owner.ObjectOwner -> {
                putJsonObject("owner") {
                  put("ObjectOwner", (oc.owner as Owner.ObjectOwner).address)
                }
              }
              is Owner.AddressOwner -> {
                putJsonObject("owner") {
                  put("AddressOwner", (oc.owner as Owner.AddressOwner).address)
                }
              }
            }
            put("objectType", oc.objectType)
            put("objectId", oc.objectId)
            put("version", oc.version)
            put("previousVersion", oc.previousVersion)
            put("digest", oc.digest)
          }
        }
      )
    encoder.encodeJsonElement(jsonArray)
  }

  override fun deserialize(decoder: Decoder): List<ObjectChange> {
    require(decoder is JsonDecoder)
    val jsonArray = decoder.decodeJsonElement().jsonArray
    return jsonArray.map { jsonElement ->
      val jsonObject = jsonElement.jsonObject
      when (jsonObject["type"]?.jsonPrimitive?.content) {
        "created" -> {
          ObjectChange.CreatedObject(
            jsonObject["type"]?.jsonPrimitive?.content ?: "type",
            jsonObject["sender"]?.jsonPrimitive?.content ?: "sender",
            if (jsonElement.jsonObject["owner"] is JsonObject) {
              whichOwner(jsonObject["owner"]?.jsonObject)
            } else {
              Owner.LiteralOwner(jsonObject["owner"]?.jsonPrimitive?.content ?: "default")
            },
            jsonObject["objectType"]?.jsonPrimitive?.content ?: "objectType",
            jsonObject["objectId"]?.jsonPrimitive?.content ?: "objectId",
            jsonObject["version"]?.jsonPrimitive?.long ?: 0,
            jsonObject["previousVersion"]?.jsonPrimitive?.long ?: 0,
            jsonObject["digest"]?.jsonPrimitive?.content ?: "digest"
          )
        }
        "mutated" -> {
          ObjectChange.MutatedObject(
            jsonObject["type"]?.jsonPrimitive?.content ?: "type",
            jsonObject["sender"]?.jsonPrimitive?.content ?: "sender",
            if (jsonElement is JsonObject) {
              whichOwner(jsonObject)
            } else {
              Owner.LiteralOwner(jsonElement.jsonPrimitive.content)
            },
            jsonObject["objectType"]?.jsonPrimitive?.content ?: "objectType",
            jsonObject["objectId"]?.jsonPrimitive?.content ?: "objectId",
            jsonObject["version"]?.jsonPrimitive?.long ?: 0,
            jsonObject["previousVersion"]?.jsonPrimitive?.long ?: 0,
            jsonObject["digest"]?.jsonPrimitive?.content ?: "digest"
          )
        }
        else -> {
          ObjectChange.DefaultObject()
        }
      }
    }
  }
}
