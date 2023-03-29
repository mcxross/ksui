package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionsPage(
  val data: List<String>,
  val nextCursor: String,
)
