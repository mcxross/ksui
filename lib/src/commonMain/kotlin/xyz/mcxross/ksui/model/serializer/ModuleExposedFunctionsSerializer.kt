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
import xyz.mcxross.ksui.model.DataTransactionInput
import xyz.mcxross.ksui.model.ModuleExposedFunction
import xyz.mcxross.ksui.model.MoveFunctionParameter
import xyz.mcxross.ksui.model.MoveNormalizedFunction

object ModuleExposedFunctionsSerializer : KSerializer<List<ModuleExposedFunction>> {
 override val descriptor: SerialDescriptor =
    ListSerializer(ModuleExposedFunction.serializer()).descriptor

  override fun serialize(encoder: Encoder, value: List<ModuleExposedFunction>) {
    require(encoder is JsonEncoder)
    TODO("Not yet implemented")
  }

  override fun deserialize(decoder: Decoder): List<ModuleExposedFunction> {
    require(decoder is JsonDecoder)
    val jsonObject = decoder.decodeJsonElement().jsonObject
    return jsonObject.keys.map {
      val functionObject = jsonObject[it]
      ModuleExposedFunction(it, MoveNormalizedFunction(
        functionObject?.jsonObject?.get("visibility")?.jsonPrimitive?.content ?: "ksui-default",
        functionObject?.jsonObject?.get("isEntry")?.jsonPrimitive?.boolean ?: false,
        functionObject?.jsonObject?.get("typeParameters")?.jsonArray?.map {""} ?: emptyList(),
          functionObject?.jsonObject?.get("parameters")?.jsonArray?.map { MoveFunctionParameter.Undefined() } ?: emptyList(),
        functionObject?.jsonObject?.get("typeParameters")?.jsonArray?.map {
          MoveFunctionParameter.Undefined()
        } ?: emptyList(),
      ))
    }
  }
}
