package xyz.mcxross.ksui.model

import kotlin.reflect.safeCast
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.EventFilterSerializer
import xyz.mcxross.ksui.model.serializer.EventParsedJsonSerializer
import xyz.mcxross.ksui.model.serializer.EventResponseSerializer
import xyz.mcxross.ksui.model.serializer.ToStringSerializer

@Serializable data class EventID(val txDigest: String, val eventSeq: Long)

@Serializable
data class EventEnvelope(
    val id: EventID,
    val packageId: String,
    val transactionModule: String,
    val sender: String,
    val type: String,
    val parsedJson: EventParsedJson,
    val bcs: String,
    val timestampMs: String,
)

@Serializable(with = EventParsedJsonSerializer::class)
data class EventParsedJson(
    val data: String,
)

@Serializable
data class Event(
    val id: EventID,
    val packageId: String,
    val transactionModule: String,
    val sender: String,
    val type: String,
    @Serializable(with = ToStringSerializer::class) val parsedJson: String,
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

enum class EventTypes {
  MOVE,
  PUBLISH,
  TRANSFER,
  DELETE,
  NEW,
  EPOCH,
  CHECKPOINT
}

@Serializable(with = EventFilterSerializer::class)
open class EventFilter {
  @Serializable class All : EventFilter()
  @Serializable data class Transaction(val digest: Digest = Digest("")) : EventFilter()
  @Serializable
  data class MoveModule(val pakage: String = "", val module: String = "") : EventFilter()
  @Serializable data class MoveEvent(val struct: String = "") : EventFilter()
  @Serializable data class EventType(val eventType: EventTypes? = null) : EventFilter()
  @Serializable data class Sender(val sender: SuiAddress = SuiAddress("")) : EventFilter()
  @Serializable data class Recipient(val addressOwner: SuiAddress = SuiAddress("")) : EventFilter()
  @Serializable data class Object(val digest: Digest = Digest("")) : EventFilter()
  @Serializable data class TimeRange(var start: Long = 0, var end: Long = 0) : EventFilter()
}

@Serializable(with = EventResponseSerializer::class)
sealed class EventResponse {
  data class Ok(val subscriptionId: Long) : EventResponse()
  data class Event(val eventEnvelope: EventEnvelope) : EventResponse()

  data class Error(
      val code: Int,
      val message: String,
  ) : EventResponse()
}

inline fun <reified T : EventFilter> createEventFilterFor(block: T.() -> Unit): T =
    when (T::class) {
      EventFilter.All::class -> T::class.safeCast(EventFilter.All())!!.apply(block)
      EventFilter.Transaction::class -> T::class.safeCast(EventFilter.Transaction())!!.apply(block)
      EventFilter.MoveModule::class -> T::class.safeCast(EventFilter.MoveModule())!!.apply(block)
      EventFilter.MoveEvent::class -> T::class.safeCast(EventFilter.MoveEvent())!!.apply(block)
      EventFilter.EventType::class -> T::class.safeCast(EventFilter.EventType())!!.apply(block)
      EventFilter.Sender::class -> T::class.safeCast(EventFilter.Sender())!!.apply(block)
      EventFilter.Recipient::class -> T::class.safeCast(EventFilter.Recipient())!!.apply(block)
      EventFilter.Object::class -> T::class.safeCast(EventFilter.Object())!!.apply(block)
      EventFilter.TimeRange::class -> T::class.safeCast(EventFilter.TimeRange())!!.apply(block)
      else -> throw Exception("Unknown EventFilter type")
    }
