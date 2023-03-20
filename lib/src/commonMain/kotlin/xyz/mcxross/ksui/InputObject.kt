package xyz.mcxross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InputObject(
  @SerialName("MovePackage") val movePackage: String?,
  @SerialName("ImmOrOwnedMoveObject") val immOrOwnedMoveObject: ImmOrOwnedMoveObject?
)
