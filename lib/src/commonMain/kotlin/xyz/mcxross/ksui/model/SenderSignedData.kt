package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.util.bcsEncode
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class SenderSignedData(val senderSignedTransactions: SenderSignedTransaction)

@Serializable
data class SenderSignedTransaction(
  val intentMessage: IntentMessage<TransactionData>,
  val txSignatures: List<String>,
)

typealias Txn = Envelope<SenderSignedData>

fun TransactionData.toTransaction(txSignatures: List<String>): Txn =
  Envelope(
    SenderSignedData(SenderSignedTransaction(IntentMessage(Intent.suiTransaction(), this), txSignatures))
  )

infix fun TransactionData.with(txSignatures: List<String>): Txn = toTransaction(txSignatures)

@OptIn(ExperimentalEncodingApi::class) fun Txn.data(): String = Base64.encode(bcsEncode(this.data.senderSignedTransactions.intentMessage.value))

fun Txn.signatures(): List<String> = this.data.senderSignedTransactions.txSignatures

fun Txn.content(): Pair<String, List<String>> = this.data() to this.signatures()
