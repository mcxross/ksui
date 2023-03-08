package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommitteeInfoItem(
  @SerialName("public_key") val publicKey: String,
  @SerialName("balance") val balance: Int
)

@Serializable
data class CommitteeInfo(
  val epoch: Int,
  @SerialName("protocol_version") val protocolVersion: Int,
  @SerialName("committee_info") val committeeInfo: List<CommitteeInfoItem>
)
