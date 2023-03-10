package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SuiObjectInfoRaw(
  val objectId: String,
  val version: Long,
  val digest: String,
  val type: String,
  val owner: AddressOwner,
  val previousTransaction: String,
)

@Serializable
internal data class SuiObjectResult(@SerialName("result") val value: List<SuiObjectInfoRaw>)

data class SuiObjectInfo(
  var objectId: String,
  val version: Long,
  val digest: Digest,
  val type: String,
  val owner: AddressOwner,
  val previousTransaction: Transaction,
)
