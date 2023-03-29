package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SuiCoinMetadataResult(
  @SerialName("result")
  val value: SuiCoinMetadata
)

@Serializable
data class SuiCoinMetadata(
    val decimals: UByte,
    val description: String,
    val iconUrl: String?,
    val id: ObjectID?,
    val name: String,
    val symbol: String,
)
