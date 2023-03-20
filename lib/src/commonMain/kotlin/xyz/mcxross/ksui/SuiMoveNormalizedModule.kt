package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class SuiMoveNormalizedModule(
  val address: SuiAddress,
  val exposedFunctions: List<Object>,
  val fileFormatVersion: UInt,
  val friends: List<SuiMoveModuleId>,
  val name: String,
  val structs: List<Object>
)
