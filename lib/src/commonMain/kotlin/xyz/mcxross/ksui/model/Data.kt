package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class Data(
  val transactions: List<TransactionKind>,
  val sender: String,
  val gasData: GasData,
)
