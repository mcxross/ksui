package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class SuiCoinMetadata(
  val decimals: UByte,
  val description: String,
  val iconUrl: String,
  val id: ObjectID,
  val name: String,
  val symbol: String,
)
