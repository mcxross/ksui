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
  @SerialName("package") val pakage: String,
  val module: String,
  val function: String,
  val arguments: List<String>,
)

@Serializable
data class Transfer(
  val recipient: String,
  @SerialName("objectRef") val objectReference: ObjectReference
)

@Serializable
data class PaySui(
  val coins: List<ObjectReference>,
  val recipients: List<String>,
  val amounts: List<Long>,
)

@Serializable
data class TransferSui(
  val recipient: String,
  val amount: Long,
)

@Serializable
data class PayAllSui(
  val recipient: String,
  val coins: List<ObjectReference>,
)

@Serializable
data class Publish(
  @Serializable(with = DisassembledFieldSerializer::class)
  val disassembled : Any,
)

@Serializable
data class TransactionKind(
  @SerialName("Call") val call: Call? = null,
  @SerialName("TransferObject") val transferObject: Transfer? = null,
  @SerialName("PaySui") val paySui: PaySui? = null,
  @SerialName("TransferSui") val transferSui: TransferSui? = null,
  @SerialName("PayAllSui") val payAllSui: PayAllSui? = null,
)

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
