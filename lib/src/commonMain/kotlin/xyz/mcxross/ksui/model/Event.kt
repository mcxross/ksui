package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.ToStringSerializer

@Serializable data class EventID(val txDigest: String, val eventSeq: Long)

@Serializable
data class EventEnvelope(
  val timestamp: Long,
  val txDigest: String,
  val id: EventID,
  val eventTmp: EventTmp,
)

@Serializable
data class EventParsedJson(
  @SerialName("acc_reward_per_share")
  val accRewardPerShare: String,
  @SerialName("reward_remaining")
  val rewardRemaining: String,
  @SerialName("stake_amount")
  val stakeAmount: String,
  val timestamp: String,
  @SerialName("total_staked_amount")
  val totalStakedAmount: String,
  val user: String,
  @SerialName("user_reward_amount")
  val userRewardAmount: String,
  @SerialName("user_staked_amount")
  val userStakedAmount: String
)
@Serializable
data class Event(
  val id: EventID,
  val packageId: String,
  val transactionModule: String,
  val sender: String,
  val type: String,
  @Serializable(with = ToStringSerializer::class)
  val parsedJson: String,
  val bcs: String,
)

@Serializable
abstract class EventTmp {
  @Serializable
  abstract class Basic {
    abstract val packageId: String
    abstract val transactionModule: String
    abstract val sender: String
    @Serializable
    data class CoinBalanceChange(
      override val packageId: String,
      override val transactionModule: String,
      override val sender: String,
      val changeType: String,
      val owner: Owner.AddressOwner,
      val coinType: String,
      val coinObjectId: String,
      val version: Int,
      val amount: Long,
    ) : Basic()

    @Serializable
    data class MutateObject(
      override val packageId: String,
      override val transactionModule: String,
      override val sender: String,
      val objectType: String,
      val objectId: String,
      val version: Int,
    ) : Basic()

    @Serializable
    data class NewObject(
      override val packageId: String,
      override val transactionModule: String,
      override val sender: String,
      val recipient: Owner.AddressOwner,
      val objectType: String,
      val objectId: String,
      val version: Int,
    ) : Basic()

    @Serializable
    data class MoveEvent(
      override val packageId: String,
      override val transactionModule: String,
      override val sender: String,
      val type: String?,
      val bcs: String?,
    ) : Basic()

    @Serializable
    data class TransferObject(
      override val packageId: String,
      override val transactionModule: String,
      override val sender: String,
      val recipient: Owner.AddressOwner,
      val objectType: String,
      val objectId: String,
      val version: Int,
    ) : Basic()

    @Serializable
    data class Publish(
      override val packageId: String,
      override val transactionModule: String,
      override val sender: String,
      val version: Int,
      val digest: String,
    ) : Basic()
  }

  @Serializable
  data class EventEventTmp(
    val coinBalanceChange: Basic.CoinBalanceChange? = null,
    val mutateObject: Basic.MutateObject? = null,
    val newObject: Basic.NewObject? = null,
    val moveEvent: Basic.MoveEvent? = null,
    val transferObject: Basic.TransferObject? = null,
    val publish: Basic.Publish? = null,
  ) : EventTmp()
}
