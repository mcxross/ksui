package xyz.mcxross.ksui.grpc.unit

import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.bytestring.ByteString
import kotlinx.rpc.grpc.GrpcStatusCode
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.registerService
import sui.rpc.v2.BatchGetObjectsRequest
import sui.rpc.v2.BatchGetObjectsResponse
import sui.rpc.v2.BatchGetTransactionsRequest
import sui.rpc.v2.BatchGetTransactionsResponse
import sui.rpc.v2.CheckpointInternal
import sui.rpc.v2.EventInternal
import sui.rpc.v2.ExecutedTransactionInternal
import sui.rpc.v2.GetCheckpointRequest
import sui.rpc.v2.GetCheckpointResponse
import sui.rpc.v2.GetEpochRequest
import sui.rpc.v2.GetEpochResponse
import sui.rpc.v2.GetObjectRequest
import sui.rpc.v2.GetObjectResponse
import sui.rpc.v2.GetProtocolConfigRequest
import sui.rpc.v2.GetProtocolConfigResponse
import sui.rpc.v2.GetServiceInfoRequest
import sui.rpc.v2.GetServiceInfoResponse
import sui.rpc.v2.GetTransactionRequest
import sui.rpc.v2.GetTransactionResponse
import sui.rpc.v2.LedgerPositionInternal
import sui.rpc.v2.LedgerService
import sui.rpc.v2.ListCheckpointsRequest
import sui.rpc.v2.ListCheckpointsResponse
import sui.rpc.v2.ListCheckpointsResponseInternal
import sui.rpc.v2.ListEventsRequest
import sui.rpc.v2.ListEventsResponse
import sui.rpc.v2.ListEventsResponseInternal
import sui.rpc.v2.ListObjectVersionsRequest
import sui.rpc.v2.ListObjectVersionsResponse
import sui.rpc.v2.ListTransactionsRequest
import sui.rpc.v2.ListTransactionsResponse
import sui.rpc.v2.ListTransactionsResponseInternal
import sui.rpc.v2.QueryEndInternal
import sui.rpc.v2.QueryEndReason
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse
import sui.rpc.v2.SubscribeCheckpointsResponseInternal
import sui.rpc.v2.SubscribeEventsRequest
import sui.rpc.v2.SubscribeEventsResponse
import sui.rpc.v2.SubscribeEventsResponseInternal
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.SubscribeTransactionsResponse
import sui.rpc.v2.SubscribeTransactionsResponseInternal
import sui.rpc.v2.SubscriptionService
import sui.rpc.v2.WatermarkInternal
import sui.rpc.v2.presence
import xyz.mcxross.ksui.core.util.runBlocking

/** (checkpoint, transaction index, event index), ordered like ledger keys and positions. */
internal data class Key(val c: ULong, val t: ULong = 0u, val e: UInt = 0u) : Comparable<Key> {
  override fun compareTo(other: Key): Int =
    compareValuesBy(this, other, { it.c }, { it.t }, { it.e })

  fun encode(): ByteString = ByteString("$c/$t/$e".encodeToByteArray())

  companion object {
    fun decode(cursor: ByteString): Key =
      cursor.toByteArray().decodeToString().split('/').let {
        Key(it[0].toULong(), it[1].toULong(), it[2].toUInt())
      }
  }
}

/**
 * A synthetic chain served over gRPC with the documented sui.rpc.v2 streaming contract:
 * subscriptions start at [tip], filtered streams open with a progress-only frame, every frame
 * carries a resume point, and List scans stop at [indexedTip] with LEDGER_TIP.
 *
 * With a filter, only checkpoints whose number is divisible by 3, transaction index 0 and event
 * index 0 match, so filtered streams have gaps that need progress frames.
 */
internal class FakeLedger(
  @Volatile var tip: ULong = 100u,
  @Volatile var indexedTip: ULong = 100u,
  private val txPerCheckpoint: ULong = 2u,
  private val eventsPerTx: UInt = 2u,
  @Volatile var listPageSize: Int = Int.MAX_VALUE,
  @Volatile var frameDelay: Duration = 2.milliseconds,
) {
  /** Fail each new subscription with UNAVAILABLE after this many frames. Null disables it. */
  @Volatile var failSubscriptionsAfterFrames: Int? = null

  /** Checkpoints that execute while no subscription is open, before the next one starts. */
  @Volatile var checkpointsBetweenSubscriptions: ULong = 0u

  /**
   * When set, the List index no longer follows the chain: it only moves this many checkpoints
   * towards [tip] on each List call, so scans end with LEDGER_TIP until it catches up.
   */
  @Volatile var indexCatchUpPerScan: ULong? = null

  /** Fail every subscription with this status before its first frame. */
  @Volatile var subscribeError: GrpcStatusCode? = null

  val scans = AtomicInteger()

  val subscriptions = AtomicInteger()
  val requests: MutableList<Any> = Collections.synchronizedList(mutableListOf())

  private fun checkpointMatches(c: ULong) = c % 3u == 0uL

  private fun nextSubscriptionStart(): ULong {
    subscribeError?.let { throw it.asException("rejected") }
    if (subscriptions.getAndIncrement() > 0) tip += checkpointsBetweenSubscriptions
    if (indexCatchUpPerScan == null && indexedTip < tip - 1u) indexedTip = tip - 1u
    return tip
  }

  private fun onScan() {
    scans.incrementAndGet()
    indexCatchUpPerScan?.let { step -> indexedTip = minOf(tip, indexedTip + step) }
  }

  private suspend fun frame(count: Int) {
    delay(frameDelay)
    failSubscriptionsAfterFrames?.let {
      if (count >= it) throw GrpcStatusCode.UNAVAILABLE.asException("connection reset")
    }
  }

  private fun advanceTo(c: ULong) {
    if (tip < c) tip = c
    if (indexCatchUpPerScan == null && indexedTip < c) indexedTip = c
  }

  private fun watermark(position: Key, covered: ULong?) =
    WatermarkInternal().apply {
      cursor = position.encode()
      covered?.let { checkpoint = it }
      cursorPosition =
        LedgerPositionInternal().apply {
          checkpoint = position.c
          if (position.t > 0u || position.e > 0u) transactionIndex = position.t
          if (position.e > 0u) eventIndex = position.e
        }
    }

  val subscriptionService =
    object : SubscriptionService {
      override fun SubscribeCheckpoints(
        message: SubscribeCheckpointsRequest,
      ): Flow<SubscribeCheckpointsResponse> = flow {
        requests += message
        val filtered = message.presence.hasFilter
        val start = nextSubscriptionStart()
        var frames = 0
        if (filtered) {
          emit(SubscribeCheckpointsResponseInternal().apply { cursor = start - 1u })
          frame(++frames)
        }
        var c = start
        while (true) {
          advanceTo(c)
          emit(
            SubscribeCheckpointsResponseInternal().apply {
              cursor = c
              if (!filtered || checkpointMatches(c)) {
                checkpoint = CheckpointInternal().apply { sequenceNumber = c }
              }
            },
          )
          frame(++frames)
          c++
        }
      }

      override fun SubscribeTransactions(
        message: SubscribeTransactionsRequest,
      ): Flow<SubscribeTransactionsResponse> = flow {
        requests += message
        val filtered = message.presence.hasFilter
        val start = nextSubscriptionStart()
        var frames = 0
        emit(
          SubscribeTransactionsResponseInternal().apply {
            watermark = watermark(Key(start), start - 1u)
          },
        )
        frame(++frames)
        var c = start
        while (true) {
          advanceTo(c)
          for (t in 0uL until txPerCheckpoint) {
            if (filtered && t != 0uL) continue
            emit(
              SubscribeTransactionsResponseInternal().apply {
                transaction = transactionAt(Key(c, t))
                watermark = watermark(Key(c, t + 1u), if (t == txPerCheckpoint - 1u) c else c - 1u)
              },
            )
            frame(++frames)
          }
          if (filtered) {
            emit(
              SubscribeTransactionsResponseInternal().apply {
                watermark = watermark(Key(c + 1u), c)
              },
            )
            frame(++frames)
          }
          c++
        }
      }

      override fun SubscribeEvents(message: SubscribeEventsRequest): Flow<SubscribeEventsResponse> =
        flow {
          requests += message
          val filtered = message.presence.hasFilter
          val start = nextSubscriptionStart()
          var frames = 0
          emit(
            SubscribeEventsResponseInternal().apply {
              watermark = watermark(Key(start), start - 1u)
            },
          )
          frame(++frames)
          var c = start
          while (true) {
            advanceTo(c)
            for (t in 0uL until txPerCheckpoint) for (e in 0u until eventsPerTx) {
              if (filtered && e != 0u) continue
              emit(
                SubscribeEventsResponseInternal().apply {
                  event = eventAt(Key(c, t, e))
                  watermark = watermark(Key(c, t, e + 1u), c - 1u)
                },
              )
              frame(++frames)
            }
            c++
          }
        }
    }

  private fun transactionAt(k: Key) =
    ExecutedTransactionInternal().apply {
      digest = "tx-${k.c}-${k.t}"
      checkpoint = k.c
      transactionIndex = k.t
    }

  private fun eventAt(k: Key) =
    EventInternal().apply {
      checkpoint = k.c
      transactionIndex = k.t
      eventIndex = k.e
      eventType = "0x2::fake::Event${k.c}_${k.t}_${k.e}"
    }

  /** Keys in `[start, end)` that a List scan can see, in ledger order. */
  private fun keys(start: Key, end: Key, level: Int, filtered: Boolean): Sequence<Key> = sequence {
    var c = start.c
    while (Key(c) < end && c <= indexedTip) {
      when (level) {
        0 -> if (!filtered || checkpointMatches(c)) yield(Key(c))
        1 ->
          for (t in 0uL until txPerCheckpoint) {
            val k = Key(c, t)
            if (k >= start && k < end && (!filtered || t == 0uL)) yield(k)
          }
        else ->
          for (t in 0uL until txPerCheckpoint) for (e in 0u until eventsPerTx) {
            val k = Key(c, t, e)
            if (k >= start && k < end && (!filtered || e == 0u)) yield(k)
          }
      }
      c++
    }
  }

  private fun <R> scan(
    start: Key,
    end: Key,
    level: Int,
    filtered: Boolean,
    item: (Key, Boolean) -> R,
    terminal: (Key, QueryEndReason) -> R,
  ): List<R> {
    val out = mutableListOf<R>()
    var delivered = 0
    for (k in keys(start, end, level, filtered)) {
      delivered++
      val last = delivered == listPageSize
      out += item(k, last)
      if (last) return out
    }
    val reachedEnd = end.c <= indexedTip + 1u
    return out +
      terminal(
        if (reachedEnd) end else Key(indexedTip + 1u),
        if (reachedEnd) QueryEndReason.CHECKPOINT_BOUND else QueryEndReason.LEDGER_TIP,
      )
  }

  private fun next(k: Key, level: Int) =
    when (level) {
      0 -> Key(k.c + 1u)
      1 -> Key(k.c, k.t + 1u)
      else -> Key(k.c, k.t, k.e + 1u)
    }

  val ledgerService =
    object : LedgerService {
      override fun ListCheckpoints(message: ListCheckpointsRequest): Flow<ListCheckpointsResponse> =
        flow {
          requests += message
          onScan()
          val start = Key(message.startCheckpoint ?: 0u)
          val end = Key(message.endCheckpoint ?: ULong.MAX_VALUE)
          scan(
              start,
              end,
              0,
              message.presence.hasFilter,
              { k, last ->
                ListCheckpointsResponseInternal().apply {
                  checkpoint = CheckpointInternal().apply { sequenceNumber = k.c }
                  watermark = watermark(next(k, 0), k.c)
                  if (last)
                    this.end = QueryEndInternal().apply { reason = QueryEndReason.ITEM_LIMIT }
                }
              },
              { p, reason ->
                ListCheckpointsResponseInternal().apply {
                  watermark = watermark(p, p.c - 1u)
                  this.end = QueryEndInternal().apply { this.reason = reason }
                }
              },
            )
            .forEach { emit(it) }
        }

      override fun ListTransactions(
        message: ListTransactionsRequest,
      ): Flow<ListTransactionsResponse> = flow {
        requests += message
        onScan()
        var start = Key(message.startCheckpoint ?: 0u, message.startTransactionIndex ?: 0u)
        message.options.after?.let { start = maxOf(start, Key.decode(it)) }
        val end =
          message.endTransactionIndex?.let { Key((message.endCheckpoint ?: 1u) - 1u, it) }
            ?: Key(message.endCheckpoint ?: ULong.MAX_VALUE)
        scan(
            start,
            end,
            1,
            message.presence.hasFilter,
            { k, last ->
              ListTransactionsResponseInternal().apply {
                transaction = transactionAt(k)
                watermark = watermark(next(k, 1), k.c - 1u)
                if (last) this.end = QueryEndInternal().apply { reason = QueryEndReason.ITEM_LIMIT }
              }
            },
            { p, reason ->
              ListTransactionsResponseInternal().apply {
                watermark = watermark(p, p.c - 1u)
                this.end = QueryEndInternal().apply { this.reason = reason }
              }
            },
          )
          .forEach { emit(it) }
      }

      override fun ListEvents(message: ListEventsRequest): Flow<ListEventsResponse> = flow {
        requests += message
        onScan()
        var start =
          Key(
            message.startCheckpoint ?: 0u,
            message.startTransactionIndex ?: 0u,
            message.startEventIndex ?: 0u,
          )
        message.options.after?.let { start = maxOf(start, Key.decode(it)) }
        val end =
          if (message.endTransactionIndex != null || message.endEventIndex != null) {
            Key(
              (message.endCheckpoint ?: 1u) - 1u,
              message.endTransactionIndex ?: 0u,
              message.endEventIndex ?: 0u,
            )
          } else {
            Key(message.endCheckpoint ?: ULong.MAX_VALUE)
          }
        scan(
            start,
            end,
            2,
            message.presence.hasFilter,
            { k, last ->
              ListEventsResponseInternal().apply {
                event = eventAt(k)
                watermark = watermark(next(k, 2), k.c - 1u)
                if (last) this.end = QueryEndInternal().apply { reason = QueryEndReason.ITEM_LIMIT }
              }
            },
            { p, reason ->
              ListEventsResponseInternal().apply {
                watermark = watermark(p, p.c - 1u)
                this.end = QueryEndInternal().apply { this.reason = reason }
              }
            },
          )
          .forEach { emit(it) }
      }

      private fun unimplemented(): Nothing = throw GrpcStatusCode.UNIMPLEMENTED.asException("fake")

      override suspend fun GetServiceInfo(message: GetServiceInfoRequest): GetServiceInfoResponse =
        unimplemented()

      override suspend fun GetObject(message: GetObjectRequest): GetObjectResponse = unimplemented()

      override suspend fun BatchGetObjects(
        message: BatchGetObjectsRequest,
      ): BatchGetObjectsResponse = unimplemented()

      override fun ListObjectVersions(
        message: ListObjectVersionsRequest,
      ): Flow<ListObjectVersionsResponse> = flow { unimplemented() }

      override suspend fun GetTransaction(message: GetTransactionRequest): GetTransactionResponse =
        unimplemented()

      override suspend fun BatchGetTransactions(
        message: BatchGetTransactionsRequest,
      ): BatchGetTransactionsResponse = unimplemented()

      override suspend fun GetCheckpoint(message: GetCheckpointRequest): GetCheckpointResponse =
        unimplemented()

      override suspend fun GetEpoch(message: GetEpochRequest): GetEpochResponse = unimplemented()

      override suspend fun GetProtocolConfig(
        message: GetProtocolConfigRequest,
      ): GetProtocolConfigResponse = unimplemented()
    }

  fun <T> serve(block: (port: Int) -> T): T {
    val port = unusedTcpPort()
    val server =
      GrpcServer(port) {
          services {
            registerService<SubscriptionService> { subscriptionService }
            registerService<LedgerService> { ledgerService }
          }
        }
        .start()
    try {
      return block(port)
    } finally {
      server.shutdown()
      runBlocking { server.awaitTermination(kotlin.time.Duration.parse("2s")) }
    }
  }
}
