package xyz.mcxross.ksui.model

import kotlin.reflect.safeCast
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.exception.UnknownEventFilterException
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

enum class Operator {
  AND,
  OR,
  ALL,
  ANY,
}

@Serializable(with = EventFilterSerializer::class)
open class EventFilter {
  @Serializable class All : EventFilter()
  @Serializable data class Transaction(val digest: Digest) : EventFilter()
  @Serializable data class MoveModule(val pakage: String, val module: String) : EventFilter()
  @Serializable data class MoveEvent(val struct: String) : EventFilter()
  @Serializable data class Sender(val address: SuiAddress) : EventFilter()
  @Serializable data class Package(val id: String) : EventFilter()
  @Serializable data class MoveEventType(val eventType: String) : EventFilter()
  @Serializable data class DataField(val path: String, val value: String)
  @Serializable data class MoveEventField(val dataField: DataField) : EventFilter()
  @Serializable data class TimeRange(val start: Long, val end: Long) : EventFilter()
  @Serializable
  data class Combined(val operator: Operator = Operator.ALL, val filters: List<EventFilter>) :
      EventFilter()
}

abstract class EventFilterMutable {

  abstract fun toImmutable(): EventFilter
  class All : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.All()
  }
  data class Transaction(var digest: Digest = Digest("")) : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.Transaction(digest)
  }
  data class MoveModule(var pakage: String = "", var module: String = "") : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.MoveModule(pakage, module)
  }
  data class MoveEvent(var struct: String = "") : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.MoveEvent(struct)
  }

  data class Sender(var address: SuiAddress = SuiAddress("")) : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.Sender(address)
  }
  data class Package(var id: String = "") : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.Package(id)
  }
  data class MoveEventType(var eventType: String = "") : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.MoveEventType(eventType)
  }
  data class DataField(var path: String = "", var value: String = "") {
    fun toMutable(): EventFilter.DataField = EventFilter.DataField(path, value)
  }
  data class MoveEventField(var dataField: DataField = DataField()) : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.MoveEventField(dataField.toMutable())
  }
  data class TimeRange(var start: Long = 0, var end: Long = 0) : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.TimeRange(start, end)
  }
  data class Combined(
      var operator: Operator = Operator.ALL,
      var filters: List<EventFilter> = emptyList()
  ) : EventFilterMutable() {
    override fun toImmutable(): EventFilter = EventFilter.Combined(operator, filters)
  }
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

/**
 * Creates an [EventFilter] instance of type [T] and applies the specified [block] on it.
 *
 * @param block A lambda function that takes an instance of [T] (subtype of [EventFilter]) and
 *   applies operations on it.
 * @return An instance of [T] with the operations from the [block] applied on it.
 * @throws UnknownEventFilterException if the [T] type is not a known [EventFilter] type.
 */
inline fun <reified T : EventFilterMutable> eventFilterFor(block: T.() -> Unit): EventFilter =
    when (T::class) {
      EventFilterMutable.All::class -> {
        val eventFilterAllMutable = T::class.safeCast(EventFilterMutable.All())!!.apply(block)
        eventFilterAllMutable.toImmutable()
      }
      EventFilterMutable.Transaction::class -> {
        val eventFilterTransactionMutable =
            T::class.safeCast(EventFilterMutable.Transaction())!!.apply(block)
        eventFilterTransactionMutable.toImmutable()
      }
      EventFilterMutable.MoveModule::class -> {
        val eventFilterMoveModuleMutable =
            T::class.safeCast(EventFilterMutable.MoveModule())!!.apply(block)
        eventFilterMoveModuleMutable.toImmutable()
      }
      EventFilterMutable.MoveEvent::class -> {
        val eventFilterMoveEventMutable =
            T::class.safeCast(EventFilterMutable.MoveEvent())!!.apply(block)
        eventFilterMoveEventMutable.toImmutable()
      }
      EventFilterMutable.Sender::class -> {
        val eventFilterSenderMutable = T::class.safeCast(EventFilterMutable.Sender())!!.apply(block)
        eventFilterSenderMutable.toImmutable()
      }
      EventFilterMutable.Package::class -> {
        val eventFilterPackageMutable =
            T::class.safeCast(EventFilterMutable.Package())!!.apply(block)
        eventFilterPackageMutable.toImmutable()
      }
      EventFilterMutable.MoveEventType::class -> {
        val eventFilterMoveEventTypeMutable =
            T::class.safeCast(EventFilterMutable.MoveEventType())!!.apply(block)
        eventFilterMoveEventTypeMutable.toImmutable()
      }
      EventFilterMutable.MoveEventField::class -> {
        val eventFilterMoveEventFieldMutable =
            T::class.safeCast(EventFilterMutable.MoveEventField())!!.apply(block)
        eventFilterMoveEventFieldMutable.toImmutable()
      }
      EventFilterMutable.TimeRange::class -> {
        val eventFilterTimeRangeMutable =
            T::class.safeCast(EventFilterMutable.TimeRange())!!.apply(block)
        eventFilterTimeRangeMutable.toImmutable()
      }
      EventFilterMutable.Combined::class -> {
        val eventFilterCombinedMutable =
            T::class.safeCast(EventFilterMutable.Combined())!!.apply(block)
        eventFilterCombinedMutable.toImmutable()
      }
      else -> throw UnknownEventFilterException("Unknown EventFilter type: ${T::class}")
    }
