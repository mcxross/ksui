package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class Balance(
  val coinType: String,
  val coinObjectCount: Int,
  val totalBalance: Long,
  val lockedBalance: Balance?
)
