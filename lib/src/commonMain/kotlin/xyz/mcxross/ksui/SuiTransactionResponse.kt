package xyz.mcxross.ksui

import kotlinx.serialization.Serializable
import org.gciatto.kt.math.BigInteger

@Serializable
data class SuiTransactionResponse(
  val checkpoint: BigInteger? = null,
  val confirmedLocalExecution: Boolean? = null,
  val effects: TransactionEffects,
  val events: List<Event>,
  val timestampMs: BigInteger,
  val transaction: Transaction,
)
