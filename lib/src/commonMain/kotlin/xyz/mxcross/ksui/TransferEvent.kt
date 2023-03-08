package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferEvent(
  @SerialName("transferObject") val transferObject: TransferObject,
  val packageId: String,
  val transactionModule: String,
  val sender: String
)
