package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.ModuleExposedFunctionsSerializer
import xyz.mcxross.ksui.model.serializer.MoveFunctionArgTypeSerializer
import xyz.mcxross.ksui.model.serializer.MoveNormalizedFunctionParameterSerializer
import xyz.mcxross.ksui.model.serializer.ToStringSerializer

@Serializable(with = MoveFunctionArgTypeSerializer::class)
abstract class MoveFunctionArgType {
  @Serializable
  data class MoveFunctionArgDefault(
    val default: String,
  ) : MoveFunctionArgType()

  @Serializable
  data class MoveFunctionArgObject(
    val objekt: String,
  ) : MoveFunctionArgType()

  @Serializable
  data class MoveFunctionArgString(
    val str: String,
  ) : MoveFunctionArgType()
}

@Serializable(with = MoveNormalizedFunctionParameterSerializer::class)
abstract class MoveFunctionParameter {

  @Serializable
  class U8 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U8"
    }
  }

  @Serializable
  class U64 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U64"
    }
  }

  @Serializable
  class U128 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U128"
    }
  }

  @Serializable
  class U256 : MoveFunctionParameter() {
    override fun toString(): String {
      return "U256"
    }
  }

  @Serializable
  class Undefined : MoveFunctionParameter() {
    override fun toString(): String {
      return "Undefined"
    }
  }

  @Serializable
  data class Struct(
    val address: String,
    val module: String,
    val name: String,
  ) : MoveFunctionParameter()

  @Serializable
  class MutableReference : MoveFunctionParameter() {
    val struct: Struct? = null

    override fun toString(): String {
      return "MutableReference"
    }

    override fun hashCode(): Int {
      return struct?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false

      other as MutableReference

      if (struct != other.struct) return false

      return true
    }
  }

  @Serializable
  data class Vector(
    val of: MoveFunctionParameter,
  ) : MoveFunctionParameter()

  @Serializable
  class Address : MoveFunctionParameter() {
    override fun toString(): String {
      return "Address"
    }
  }

  override fun toString(): String {
    return "MoveFunctionParameter"
  }
}

@Serializable data class SuiMoveAbilitySet(val abilities: List<String> = emptyList())

@Serializable
data class MoveNormalizedFunction(
  val visibility: String,
  val isEntry: Boolean,
  val typeParameters: List<SuiMoveAbilitySet> = emptyList(),
  val parameters: List<MoveFunctionParameter> = emptyList(),
  @SerialName("return") val returnType: List<MoveFunctionParameter>,
)

@Serializable
data class Friend(
  val address: String,
  val name: String,
)

@Serializable
data class ModuleExposedFunction(val name: String, val normalized: MoveNormalizedFunction)

@Serializable
data class MoveNormalizedModule(
  val fileFormatVersion: Long,
  val address: String,
  val name: String,
  val friends: List<Friend> = emptyList(),
  @Serializable(with = ToStringSerializer::class) val structs: String,
  @Serializable(with = ModuleExposedFunctionsSerializer::class)
  val exposedFunctions: List<ModuleExposedFunction> = emptyList(),
)
