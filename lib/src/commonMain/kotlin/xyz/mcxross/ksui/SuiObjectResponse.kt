package xyz.mcxross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Owner(@SerialName("AddressOwner") val address: SuiAddress)

@Serializable data class Fields(val balance: Balance, val id: ID)

@Serializable
data class Content(
  val dataType: String,
  val type: String,
  val hasPublicTransfer: Boolean,
  val fields: Fields
)

@Serializable
data class Details(
  val objectId: String,
  val version: Int,
  val digest: Digest,
  val type: String,
  val owner: Owner,
  val previousTransaction: Transaction,
  val storageRebate: Int,
  val content: Content
)

@Serializable
data class SuiObjectResponse(val status: Status, val details: Details)
