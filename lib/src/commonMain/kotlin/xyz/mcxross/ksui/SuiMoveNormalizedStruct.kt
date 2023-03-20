package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class SuiMoveNormalizedStruct(
  val abilities: List<SuiMoveAbilitySet>,
  val fields: List<SuiMoveNormalizedField>,
  val typeParameters: List<SuiMoveStructTypeParameter>
)
