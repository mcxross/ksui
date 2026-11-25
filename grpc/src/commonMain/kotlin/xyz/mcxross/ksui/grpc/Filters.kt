package xyz.mcxross.ksui.grpc

import sui.rpc.v2.AffectedAddressFilter
import sui.rpc.v2.AffectedObjectFilter
import sui.rpc.v2.EmitModuleFilter
import sui.rpc.v2.EventFilter
import sui.rpc.v2.EventLiteral
import sui.rpc.v2.EventStreamHeadFilter
import sui.rpc.v2.EventTerm
import sui.rpc.v2.EventTypeFilter
import sui.rpc.v2.MoveCallFilter
import sui.rpc.v2.PackageWriteFilter
import sui.rpc.v2.SenderFilter
import sui.rpc.v2.TransactionFilter
import sui.rpc.v2.TransactionLiteral
import sui.rpc.v2.TransactionTerm
import sui.rpc.v2.invoke
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.ObjectId

@DslMarker annotation class StreamFilterDsl

/**
 * Builds a filter for [xyz.mcxross.ksui.grpc.protocol.Subscription.transactionStream],
 * [xyz.mcxross.ksui.grpc.protocol.Subscription.checkpointStream] and the raw transaction and
 * checkpoint calls.
 *
 * Conditions written directly in the block must all hold. `anyOf { }` adds alternatives, of which
 * at least one must hold; each alternative is one condition or an `allOf { }` group. `not { }`
 * negates one condition.
 *
 * ```kotlin
 * transactionFilter {
 *     sender(me)
 *     anyOf {
 *         moveCall("0x2::coin")
 *         allOf { affectedObject(nft); not { emitModule("0xabc::market") } }
 *     }
 * }
 * ```
 *
 * Addresses are written in canonical form, and Move paths and types are checked here, so a
 * malformed filter fails when it is built rather than at the server.
 *
 * @throws IllegalArgumentException if the block adds no condition or a value is malformed.
 */
fun transactionFilter(block: TransactionFilterScope.() -> Unit): TransactionFilter {
  val root = TransactionScope(ScopeKind.ROOT).apply(block)
  val terms = root.terms()
  return TransactionFilter {
    this.terms =
      terms.map { literals ->
        TransactionTerm { this.literals = literals.map { it.toTransactionLiteral() } }
      }
  }
}

/**
 * Builds a filter for [xyz.mcxross.ksui.grpc.protocol.Subscription.eventStream] and the raw event
 * calls, with the same rules as [transactionFilter]. Only the predicates that apply to events are available.
 *
 * @throws IllegalArgumentException if the block adds no condition or a value is malformed.
 */
fun eventFilter(block: EventFilterScope.() -> Unit): EventFilter {
  val root = EventScope(ScopeKind.ROOT).apply(block)
  val terms = root.terms()
  return EventFilter {
    this.terms = terms.map { literals -> EventTerm { this.literals = literals.map { it.toEventLiteral() } } }
  }
}

/** Conditions that apply to both transactions and events. */
@StreamFilterDsl
interface EventConditions {
  /** Sent by [address]. In an event filter, every event of such a transaction matches. */
  fun sender(address: AccountAddress)

  fun sender(address: String) = sender(AccountAddress.fromString(address))

  /**
   * Emitted an event from [path], `package[::module]`. This is the top-level Move call that
   * triggered the event.
   */
  fun emitModule(path: String)

  /**
   * Emitted an event of [type], `address[::module[::Name[<T, ...>]]]`. Shorter forms match more:
   * `0x2::coin::CoinEvent` matches any instantiation, and `0x2::coin::CoinEvent<0x2::sui::SUI>`
   * only that one.
   */
  fun eventType(type: String)

  /** Wrote to the authenticated event stream [streamId]. */
  fun eventStreamHead(streamId: AccountAddress)

  fun eventStreamHead(streamId: String) = eventStreamHead(AccountAddress.fromString(streamId))
}

/** Conditions that apply to transactions (and checkpoints that contain them). */
@StreamFilterDsl
interface TransactionConditions : EventConditions {
  /**
   * Changed state for [address] as a side effect: it owns an object after the transaction, owned
   * one that was mutated, transferred, deleted or wrapped, or its address balance changed.
   */
  fun affectedAddress(address: AccountAddress)

  fun affectedAddress(address: String) = affectedAddress(AccountAddress.fromString(address))

  /** Its effects include a change to [objectId]. */
  fun affectedObject(objectId: AccountAddress)

  fun affectedObject(objectId: ObjectId) = affectedObject(objectId.hash)

  fun affectedObject(objectId: String) = affectedObject(AccountAddress.fromString(objectId))

  /** Made a Move call matching [path], `package[::module[::function]]`. */
  fun moveCall(path: String)

  /** Published or upgraded any Move package. */
  fun packageWrite()
}

@StreamFilterDsl interface TransactionNotScope : TransactionConditions

@StreamFilterDsl
interface TransactionAllOfScope : TransactionConditions {
  /** Adds the negation of the single condition in [block]. */
  fun not(block: TransactionNotScope.() -> Unit)
}

@StreamFilterDsl
interface TransactionAnyOfScope : TransactionConditions {
  /** Adds an alternative where every condition in [block] must hold. */
  fun allOf(block: TransactionAllOfScope.() -> Unit)

  /** Adds an alternative that is the negation of the single condition in [block]. */
  fun not(block: TransactionNotScope.() -> Unit)
}

@StreamFilterDsl
interface TransactionFilterScope : TransactionAllOfScope {
  /** Requires at least one of the alternatives in [block]. Each condition is one alternative. */
  fun anyOf(block: TransactionAnyOfScope.() -> Unit)
}

@StreamFilterDsl interface EventNotScope : EventConditions

@StreamFilterDsl
interface EventAllOfScope : EventConditions {
  /** Adds the negation of the single condition in [block]. */
  fun not(block: EventNotScope.() -> Unit)
}

@StreamFilterDsl
interface EventAnyOfScope : EventConditions {
  /** Adds an alternative where every condition in [block] must hold. */
  fun allOf(block: EventAllOfScope.() -> Unit)

  /** Adds an alternative that is the negation of the single condition in [block]. */
  fun not(block: EventNotScope.() -> Unit)
}

@StreamFilterDsl
interface EventFilterScope : EventAllOfScope {
  /** Requires at least one of the alternatives in [block]. Each condition is one alternative. */
  fun anyOf(block: EventAnyOfScope.() -> Unit)
}

// --- Implementation ----------------------------------------------------------------------------

internal sealed interface Condition {
  data class Sender(val address: String) : Condition

  data class AffectedAddress(val address: String) : Condition

  data class AffectedObject(val objectId: String) : Condition

  data class MoveCall(val function: String) : Condition

  data class EmitModule(val module: String) : Condition

  data class EventType(val eventType: String) : Condition

  data class EventStreamHead(val streamId: String) : Condition

  data object PackageWrite : Condition
}

internal data class Literal(val condition: Condition, val negated: Boolean = false)

internal enum class ScopeKind {
  ROOT,
  ALL_OF,
  ANY_OF,
  NOT,
}

/** Collects conditions for one scope and expands the root into the protocol's OR-of-ANDs form. */
internal abstract class ConditionScope(private val kind: ScopeKind) {
  private val literals = mutableListOf<Literal>()
  private val alternatives = mutableListOf<List<Literal>>()
  private val groups = mutableListOf<List<List<Literal>>>()

  protected fun add(condition: Condition) {
    if (kind == ScopeKind.ANY_OF) alternatives += listOf(Literal(condition)) else literals += Literal(condition)
  }

  protected fun addNegated(scope: ConditionScope) {
    val inner = scope.literals.singleOrNull()
    require(inner != null && !inner.negated) { "not { } takes exactly one condition" }
    val negated = inner.copy(negated = true)
    if (kind == ScopeKind.ANY_OF) alternatives += listOf(negated) else literals += negated
  }

  protected fun addAllOf(scope: ConditionScope) {
    require(scope.literals.isNotEmpty()) { "allOf { } needs at least one condition" }
    alternatives += scope.literals.toList()
  }

  protected fun addAnyOf(scope: ConditionScope) {
    require(scope.alternatives.isNotEmpty()) { "anyOf { } needs at least one alternative" }
    groups += scope.alternatives.toList()
  }

  /** Root conditions joined with one alternative from every anyOf group. */
  fun terms(): List<List<Literal>> {
    require(literals.isNotEmpty() || groups.isNotEmpty()) {
      "A filter needs at least one condition; pass no filter to match everything"
    }
    return groups.fold(listOf(literals.toList())) { terms, group ->
      terms.flatMap { term -> group.map { alternative -> term + alternative } }
    }
  }

  protected fun addSender(address: AccountAddress) = add(Condition.Sender(address.toString()))

  protected fun addEmitModule(path: String) = add(Condition.EmitModule(MovePaths.path(path, 2, "emitModule")))

  protected fun addEventType(type: String) = add(Condition.EventType(MovePaths.type(type)))

  protected fun addEventStreamHead(streamId: AccountAddress) = add(Condition.EventStreamHead(streamId.toString()))
}

internal class TransactionScope(kind: ScopeKind) :
  ConditionScope(kind), TransactionFilterScope, TransactionAnyOfScope, TransactionNotScope {
  override fun sender(address: AccountAddress) = addSender(address)

  override fun emitModule(path: String) = addEmitModule(path)

  override fun eventType(type: String) = addEventType(type)

  override fun eventStreamHead(streamId: AccountAddress) = addEventStreamHead(streamId)

  override fun affectedAddress(address: AccountAddress) = add(Condition.AffectedAddress(address.toString()))

  override fun affectedObject(objectId: AccountAddress) = add(Condition.AffectedObject(objectId.toString()))

  override fun moveCall(path: String) = add(Condition.MoveCall(MovePaths.path(path, 3, "moveCall")))

  override fun packageWrite() = add(Condition.PackageWrite)

  override fun not(block: TransactionNotScope.() -> Unit) = addNegated(TransactionScope(ScopeKind.NOT).apply(block))

  override fun allOf(block: TransactionAllOfScope.() -> Unit) = addAllOf(TransactionScope(ScopeKind.ALL_OF).apply(block))

  override fun anyOf(block: TransactionAnyOfScope.() -> Unit) = addAnyOf(TransactionScope(ScopeKind.ANY_OF).apply(block))
}

internal class EventScope(kind: ScopeKind) :
  ConditionScope(kind), EventFilterScope, EventAnyOfScope, EventNotScope {
  override fun sender(address: AccountAddress) = addSender(address)

  override fun emitModule(path: String) = addEmitModule(path)

  override fun eventType(type: String) = addEventType(type)

  override fun eventStreamHead(streamId: AccountAddress) = addEventStreamHead(streamId)

  override fun not(block: EventNotScope.() -> Unit) = addNegated(EventScope(ScopeKind.NOT).apply(block))

  override fun allOf(block: EventAllOfScope.() -> Unit) = addAllOf(EventScope(ScopeKind.ALL_OF).apply(block))

  override fun anyOf(block: EventAnyOfScope.() -> Unit) = addAnyOf(EventScope(ScopeKind.ANY_OF).apply(block))
}

private fun Literal.toTransactionLiteral(): TransactionLiteral {
  val literal = this
  return TransactionLiteral {
    negated = literal.negated
    predicate =
      when (val c = literal.condition) {
        is Condition.Sender -> TransactionLiteral.Predicate.Sender(SenderFilter { address = c.address })
        is Condition.AffectedAddress ->
          TransactionLiteral.Predicate.AffectedAddress(AffectedAddressFilter { address = c.address })
        is Condition.AffectedObject ->
          TransactionLiteral.Predicate.AffectedObject(AffectedObjectFilter { objectId = c.objectId })
        is Condition.MoveCall -> TransactionLiteral.Predicate.MoveCall(MoveCallFilter { function = c.function })
        is Condition.EmitModule -> TransactionLiteral.Predicate.EmitModule(EmitModuleFilter { module = c.module })
        is Condition.EventType -> TransactionLiteral.Predicate.EventType(EventTypeFilter { eventType = c.eventType })
        is Condition.EventStreamHead ->
          TransactionLiteral.Predicate.EventStreamHead(EventStreamHeadFilter { streamId = c.streamId })
        Condition.PackageWrite -> TransactionLiteral.Predicate.PackageWrite(PackageWriteFilter {})
      }
  }
}

private fun Literal.toEventLiteral(): EventLiteral {
  val literal = this
  return EventLiteral {
    negated = literal.negated
    predicate =
      when (val c = literal.condition) {
        is Condition.Sender -> EventLiteral.Predicate.Sender(SenderFilter { address = c.address })
        is Condition.EmitModule -> EventLiteral.Predicate.EmitModule(EmitModuleFilter { module = c.module })
        is Condition.EventType -> EventLiteral.Predicate.EventType(EventTypeFilter { eventType = c.eventType })
        is Condition.EventStreamHead ->
          EventLiteral.Predicate.EventStreamHead(EventStreamHeadFilter { streamId = c.streamId })
        else -> error("$c does not apply to events")
      }
  }
}

/**
 * Strict checks and canonical forms for the Move paths and type strings filters take. Addresses
 * are written in full (`0x` and 64 hex digits).
 */
internal object MovePaths {
  private val IDENTIFIER = Regex("^(?:[A-Za-z][A-Za-z0-9_]*|_[A-Za-z0-9_]+)$")
  private val PRIMITIVES = setOf("bool", "u8", "u16", "u32", "u64", "u128", "u256", "address", "signer")

  private fun address(text: String, what: String): String =
    try {
      AccountAddress.fromString(text.trim()).toString()
    } catch (e: IllegalArgumentException) {
      throw IllegalArgumentException("$what: '$text' is not an address", e)
    }

  private fun identifier(text: String, what: String): String {
    val trimmed = text.trim()
    require(IDENTIFIER.matches(trimmed)) { "$what: '$text' is not a Move identifier" }
    return trimmed
  }

  /** `package[::segment[::segment]]` with at most [maxSegments] parts. */
  fun path(path: String, maxSegments: Int, what: String): String {
    val parts = path.split("::")
    require(parts.size in 1..maxSegments) {
      "$what: expected up to $maxSegments '::'-separated parts, got '$path'"
    }
    return (listOf(address(parts[0], what)) + parts.drop(1).map { identifier(it, what) }).joinToString("::")
  }

  /** `address[::module[::Name[<T, ...>]]]`. */
  fun type(type: String): String {
    val generic = type.indexOf('<')
    if (generic < 0) return path(type, 3, "eventType")
    val head = path(type.substring(0, generic), 3, "eventType")
    require(head.split("::").size == 3) { "eventType: type arguments need address::module::Name, got '$type'" }
    val parser = TypeParser(type, generic)
    val arguments = parser.typeArguments()
    parser.end()
    return "$head<${arguments.joinToString(", ")}>"
  }

  /** Recursive-descent parser for `<T, ...>` where T is a primitive, vector<T> or struct type. */
  private class TypeParser(private val text: String, private var at: Int) {
    private fun skipSpaces() {
      while (at < text.length && text[at].isWhitespace()) at++
    }

    private fun expect(token: String) {
      skipSpaces()
      require(text.startsWith(token, at)) { "eventType: expected '$token' at ${at + 1} in '$text'" }
      at += token.length
    }

    private fun word(): String {
      skipSpaces()
      val start = at
      while (at < text.length && (text[at].isLetterOrDigit() || text[at] == '_')) at++
      require(at > start) { "eventType: expected a name at ${start + 1} in '$text'" }
      return text.substring(start, at)
    }

    fun typeArguments(): List<String> {
      expect("<")
      val arguments = mutableListOf(typeTag())
      while (true) {
        skipSpaces()
        if (text.startsWith(",", at)) {
          at++
          arguments += typeTag()
        } else {
          break
        }
      }
      expect(">")
      return arguments
    }

    private fun typeTag(): String {
      val first = word()
      if (first in PRIMITIVES) return first
      if (first == "vector") {
        expect("<")
        val element = typeTag()
        expect(">")
        return "vector<$element>"
      }
      val address = address(first, "eventType")
      expect("::")
      val module = identifier(word(), "eventType")
      expect("::")
      val name = identifier(word(), "eventType")
      skipSpaces()
      val arguments = if (text.startsWith("<", at)) "<${typeArguments().joinToString(", ")}>" else ""
      return "$address::$module::$name$arguments"
    }

    fun end() {
      skipSpaces()
      require(at == text.length) { "eventType: unexpected '${text.substring(at)}' in '$text'" }
    }
  }
}
