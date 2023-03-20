package xyz.mcxross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class CommitteeInfo(val publicKey: String, val balance: Int)

@Serializable
data class Info(
  val epoch: Int,
  @SerialName("protocol_version") val protocolVersion: Int,
  @SerialName("committee_info") val committeeInfo: List<CommitteeInfo>
)

@Serializable data class SuiCommittee(@SerialName("result") val info: Info)
