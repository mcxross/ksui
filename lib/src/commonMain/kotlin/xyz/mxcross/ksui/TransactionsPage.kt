package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class TransactionsPage(
  val data: List<String>,
  val nextCursor: String,
)
