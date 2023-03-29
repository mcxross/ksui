package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionBytes(
  val gas: List<Gas>,
  val inputObjects: List<InputObject>,
  val txBytes: String,
)
