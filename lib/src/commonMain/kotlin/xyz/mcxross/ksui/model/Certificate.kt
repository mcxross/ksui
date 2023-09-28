package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthSignInfo(
  val epoch: Int,
  val signature: String,
  @SerialName("signers_map") val signersMap: List<Int>,
)

@Serializable
data class Certificate(
  val transactionDigest: String,
  val data: Data,
  val txSignatures: List<String>,
  val authSignInfo: AuthSignInfo,
)
