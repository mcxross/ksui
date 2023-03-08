package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class Effects(
  val messageVersion: String,
  val status: Status,
  val executedEpoch: Int,
  val gasUsed: GasUsed,
  val transactionDigest: String,
  val mutated: List<MutatedObject>,
  val gasObject: GasObject,
  val eventsDigest: String
)
