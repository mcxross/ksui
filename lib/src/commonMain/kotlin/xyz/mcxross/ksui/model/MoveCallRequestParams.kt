package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

// TODO: "Silence the noise; temporary"
@Serializable
data class MoveCallRequestParams(
  val packageObjectId: String,
  val module: String,
  val function: String,
  val typeArguments: List<TypeTag>,
  val arguments: List<String>
)
