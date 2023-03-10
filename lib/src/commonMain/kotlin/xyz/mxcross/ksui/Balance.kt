package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LockedBalance {
  override fun toString(): String {
    return "LockedBalance"
  }
}

@Serializable internal data class BalanceResult(@SerialName("result") val value: Balance)

@Serializable
data class Balance(
  val coinType: String,
  val coinObjectCount: Int,
  val totalBalance: Long,
  val lockedBalance: LockedBalance
)
