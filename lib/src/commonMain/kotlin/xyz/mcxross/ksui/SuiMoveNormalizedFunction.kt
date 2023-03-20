package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class SuiMoveNormalizedFunction(
  val isEntry: Boolean,
  val parameters: List<SuiMoveNormalizedType>,
  val return_: SuiMoveNormalizedType,
  val typeParameters: List<SuiMoveAbilitySet>,
  val visibility: SuiMoveVisibility
)
