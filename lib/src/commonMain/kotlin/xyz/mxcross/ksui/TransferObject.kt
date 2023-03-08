package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Recipient(@SerialName("AddressOwner") val addressOwner: String)

@Serializable
data class TransferObject(
  val packageId: String,
  val transactionModule: String,
  val sender: String,
  val recipient: Recipient,
  val objectType: String,
  val objectId: String,
  val version: Long
)
