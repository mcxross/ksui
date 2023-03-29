package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class TransactionDigest(val value: String)

@Serializable
data class TransactionDigests(
  @SerialName("result")
  val vec: List<String>
)
