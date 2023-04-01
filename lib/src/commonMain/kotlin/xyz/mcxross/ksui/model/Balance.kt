package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.OwnerSerializer

@Serializable
class LockedBalance {
  override fun toString(): String {
    return "LockedBalance"
  }
}

@Serializable
data class Balance(
  val coinType: String,
  val coinObjectCount: Int,
  val totalBalance: Long,
  val lockedBalance: LockedBalance
)

@Serializable
data class BalanceChange(
  @Serializable(with = OwnerSerializer::class)
  val owner: Owner,
  val coinType: String,
  val amount: String,
)
