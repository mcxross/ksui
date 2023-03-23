package xyz.mcxross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The options for the transaction response. */
@Serializable
data class TransactionResponseOptions(
  val showInput: Boolean,
  val showRawInput: Boolean,
  val showEffects: Boolean,
  val showEvents: Boolean,
  val showObjectChanges: Boolean,
  val showBalanceChanges: Boolean
)

@Serializable
data class Call(
  @SerialName("package")
  val pakage: String,
  val module: String,
  val function: String,
  val arguments: List<String>,
)

@Serializable data class TransactionKind(@SerialName("Call") val call: Call)

@Serializable
data class TransactionResponse(
  val certificate: Certificate,
  val effects: Effects?,
  @SerialName("timestamp_ms") val timestampMs: Long,
  val checkpoint: Long,
  @SerialName("parsed_data") val parsedData: Data?,
)

@Serializable
data class TransactionResponseRaw(@SerialName("result") val value: TransactionResponse)
