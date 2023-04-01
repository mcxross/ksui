package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class SuiCoinMetadata(
  val decimals: UByte,
  val description: String,
  val iconUrl: String?,
  val id: ObjectID?,
  val name: String,
  val symbol: String,
)

@Serializable data class Supply(val value: Long)
