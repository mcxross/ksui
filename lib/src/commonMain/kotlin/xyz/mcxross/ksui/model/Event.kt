package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class EventID(val txDigest: String, val eventSeq: Long)

@Serializable
data class EventEnvelope(
  val timestamp: Long,
  val txDigest: String,
  val id: EventID,
  val event: Event,
)

@Serializable
abstract class Event {
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
        val owner: AddressOwner,
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
        val recipient: AddressOwner,
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
        val recipient: AddressOwner,
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
  data class EventEvent(
    val coinBalanceChange: Basic.CoinBalanceChange? = null,
    val mutateObject: Basic.MutateObject? = null,
    val newObject: Basic.NewObject? = null,
    val moveEvent: Basic.MoveEvent? = null,
    val transferObject: Basic.TransferObject? = null,
    val publish: Basic.Publish? = null,
  ) : Event()
}
