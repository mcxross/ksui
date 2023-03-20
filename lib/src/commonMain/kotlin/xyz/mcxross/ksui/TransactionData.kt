package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class TransactionData(
  val messageVersion: String,
  val transactions: List<Transaction>,
  val sender: String,
  val gasData: GasData
)
