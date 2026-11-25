package xyz.mcxross.ksui.grpc.internal

import com.google.protobuf.kotlin.FieldMask
import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.rpc.grpc.GrpcStatusCode
import kotlinx.rpc.grpc.GrpcStatusException
import kotlinx.rpc.grpc.status
import kotlinx.rpc.grpc.statusCode
import sui.rpc.v2.Checkpoint
import sui.rpc.v2.Event
import sui.rpc.v2.EventFilter
import sui.rpc.v2.ExecutedTransaction
import sui.rpc.v2.ListCheckpointsRequestInternal
import sui.rpc.v2.ListCheckpointsResponse
import sui.rpc.v2.ListEventsRequestInternal
import sui.rpc.v2.ListEventsResponse
import sui.rpc.v2.ListTransactionsRequestInternal
import sui.rpc.v2.ListTransactionsResponse
import sui.rpc.v2.QueryEndReason
import sui.rpc.v2.QueryOptionsInternal
import sui.rpc.v2.SubscribeCheckpointsResponse
import sui.rpc.v2.SubscribeEventsResponse
import sui.rpc.v2.SubscribeTransactionsResponse
import sui.rpc.v2.TransactionFilter
import sui.rpc.v2.Watermark
import sui.rpc.v2.presence
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.grpc.StreamFrame
import xyz.mcxross.ksui.grpc.StreamPosition
import xyz.mcxross.ksui.grpc.StreamRetryPolicy

/** A frame reduced to what resuming needs: its item, if any, and the position just past it. */
internal data class Positioned<T : Any>(val item: T?, val after: StreamPosition)

/** A List scan frame: a positioned frame when it carries a watermark, and its end, if final. */
internal data class Scanned<T : Any>(val frame: Positioned<T>?, val end: QueryEndReason?)

/** One kind of resumable stream: a live subscription and the List scan that replays its gaps. */
internal class StreamSource<Live, Scan, T : Any>(
  val subscribe: () -> Flow<Live>,
  val fromLive: (Live) -> Positioned<T>,
  /** Scans `[from, until)`, resuming from `from.cursor` when it is set. */
  val scan: (from: StreamPosition, until: StreamPosition) -> Flow<Scan>,
  val fromScan: (Scan) -> Scanned<T>,
  /** The key of the item a frame carries, from the position just past it. */
  val itemKey: (after: StreamPosition) -> StreamPosition,
)

/** Frames buffered from the live stream while a gap is replayed. */
private const val LIVE_BUFFER = 8192

private class StreamEndedException : Exception("The server ended the subscription")

private val RETRYABLE_CODES =
  setOf(
    GrpcStatusCode.UNAVAILABLE,
    GrpcStatusCode.INTERNAL,
    GrpcStatusCode.UNKNOWN,
    GrpcStatusCode.ABORTED,
    GrpcStatusCode.RESOURCE_EXHAUSTED,
    GrpcStatusCode.CANCELLED,
    GrpcStatusCode.DEADLINE_EXCEEDED,
  )

private fun Exception.isRetryable(): Boolean =
  when (this) {
    is StreamEndedException -> true
    is GrpcStatusException -> status.statusCode in RETRYABLE_CODES
    else -> false
  }

private fun StreamRetryPolicy.backoff(attempt: Int): Duration {
  var delay = initialDelay
  repeat(attempt.coerceAtMost(30)) { delay = minOf(delay * 2, maxDelay) }
  return minOf(delay, maxDelay)
}

/**
 * Streams [source] from [resumeFrom] (or the live tip when `null`) without gaps or duplicates,
 * across reconnects.
 *
 * Each attempt follows the sui.rpc.v2 contract: open the live subscription first and buffer it,
 * take the position its first frame establishes, replay `[position, that start)` with the List
 * scan, then hand over to the buffered live frames. Frames at or before the last delivered
 * position are dropped by comparing ledger positions, never cursor bytes.
 */
internal fun <Live, Scan, T : Any> resumableStream(
  source: StreamSource<Live, Scan, T>,
  resumeFrom: StreamPosition?,
  retry: StreamRetryPolicy,
): Flow<Result<StreamFrame<T>, SuiError>> = flow {
  var position = resumeFrom
  var failures = 0
  while (true) {
    val error: Exception =
      try {
        coroutineScope {
          val live: ReceiveChannel<Live> = source.subscribe().buffer(LIVE_BUFFER).produceIn(this)
          val opening = live.receiveCatching()
          opening.exceptionOrNull()?.let { throw it }
          val first = source.fromLive(opening.getOrNull() ?: throw StreamEndedException())
          failures = 0

          val liveStart = if (first.item != null) source.itemKey(first.after) else first.after
          val from = position
          if (from != null && from < liveStart) {
            replay(source, from, liveStart, retry) { frame ->
              emit(Result.Ok(frame))
              position = frame.resumeFrom
            }
          }

          suspend fun deliver(frame: Positioned<T>) {
            val current = position
            if (current != null && frame.after <= current) return
            emit(Result.Ok(StreamFrame(frame.item, frame.after, replayed = false)))
            position = frame.after
          }
          deliver(first)
          for (message in live) deliver(source.fromLive(message))
        }
        StreamEndedException()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        e
      }
    if (!error.isRetryable() || failures >= retry.maxAttempts) {
      emit(Result.Err(error.toSuiError()))
      return@flow
    }
    delay(retry.backoff(failures++))
  }
}

private suspend fun <Live, Scan, T : Any> replay(
  source: StreamSource<Live, Scan, T>,
  from: StreamPosition,
  until: StreamPosition,
  retry: StreamRetryPolicy,
  sink: suspend (StreamFrame<T>) -> Unit,
) {
  var next = from
  while (next < until) {
    var end: QueryEndReason? = null
    var advanced = false
    source.scan(next, until).collect { message ->
      val scanned = source.fromScan(message)
      scanned.frame?.let { frame ->
        // The most recent cursor is always the safe resume point, even when the position repeats.
        val cursor = frame.after.cursor ?: next.cursor
        if (frame.after > next) {
          val after = if (frame.after > until) until else frame.after.copy(cursor = cursor)
          sink(StreamFrame(frame.item, after, replayed = true))
          next = after
          advanced = true
        } else {
          next = next.copy(cursor = cursor)
        }
      }
      scanned.end?.let { end = it }
    }
    when (end) {
      QueryEndReason.CHECKPOINT_BOUND,
      QueryEndReason.CURSOR_BOUND -> return
      QueryEndReason.ITEM_LIMIT,
      QueryEndReason.SCAN_LIMIT -> if (!advanced) delay(retry.ledgerTipPollInterval)
      // The index trails the live stream; wait for it to catch up.
      else -> if (next < until) delay(retry.ledgerTipPollInterval)
    }
  }
}

private fun Watermark.position(): StreamPosition? =
  if (presence.hasCursorPosition) {
    StreamPosition(
      checkpoint = cursorPosition.checkpoint ?: 0u,
      transactionIndex = cursorPosition.transactionIndex ?: 0u,
      eventIndex = cursorPosition.eventIndex ?: 0u,
      cursor = cursor,
    )
  } else {
    null
  }

private fun Watermark.coveredPosition(): StreamPosition? =
  checkpoint?.let { StreamPosition(it + 1u, cursor = cursor) }

private fun missingPosition(kind: String): Nothing =
  throw IllegalStateException(
    "The server sent a $kind frame without Watermark.cursor_position; include the item's " +
      "checkpoint and index fields in the read mask"
  )

// --- Checkpoints -------------------------------------------------------------------------------

internal fun checkpointSource(
  runtime: GrpcRuntime,
  filter: TransactionFilter?,
  readMask: FieldMask?,
): StreamSource<SubscribeCheckpointsResponse, ListCheckpointsResponse, Checkpoint> =
  StreamSource(
    subscribe = { subscribeCheckpoints(runtime, readMask, filter) },
    fromLive = { frame ->
      val cursor = frame.cursor ?: throw IllegalStateException("Checkpoint frame without a cursor")
      Positioned(frame.checkpoint.takeIf { frame.presence.hasCheckpoint }, StreamPosition(cursor + 1u))
    },
    scan = { from, until ->
      listCheckpoints(
        runtime,
        ListCheckpointsRequestInternal().apply {
          readMask?.let { this.readMask = it }
          filter?.let { this.filter = it }
          // Checkpoint scans resume by number: the last cursor + 1 is the start.
          startCheckpoint = from.checkpoint
          endCheckpoint = until.checkpoint
        },
      )
    },
    fromScan = { frame ->
      val item = frame.checkpoint.takeIf { frame.presence.hasCheckpoint }
      val after =
        frame.watermark.takeIf { frame.presence.hasWatermark }?.position()?.copy(cursor = null)
          ?: item?.sequenceNumber?.let { StreamPosition(it + 1u) }
          ?: frame.watermark.takeIf { frame.presence.hasWatermark }?.coveredPosition()?.copy(cursor = null)
      Scanned(after?.let { Positioned(item, it) }, frame.end.reason.takeIf { frame.presence.hasEnd })
    },
    itemKey = { after -> StreamPosition(after.checkpoint - 1u) },
  )

// --- Transactions ------------------------------------------------------------------------------

private fun transactionAfter(watermark: Watermark?, item: ExecutedTransaction?): StreamPosition? =
  watermark?.position()
    ?: item?.let { tx ->
      val c = tx.checkpoint ?: return@let null
      val t = tx.transactionIndex ?: return@let null
      StreamPosition(c, t + 1u, cursor = watermark?.cursor)
    }
    ?: watermark?.takeIf { item == null }?.coveredPosition()

internal fun transactionSource(
  runtime: GrpcRuntime,
  filter: TransactionFilter?,
  readMask: FieldMask?,
): StreamSource<SubscribeTransactionsResponse, ListTransactionsResponse, ExecutedTransaction> =
  StreamSource(
    subscribe = { subscribeTransactions(runtime, filter, readMask) },
    fromLive = { frame ->
      val item = frame.transaction.takeIf { frame.presence.hasTransaction }
      val watermark = frame.watermark.takeIf { frame.presence.hasWatermark }
      Positioned(item, transactionAfter(watermark, item) ?: missingPosition("transaction"))
    },
    scan = { from, until ->
      listTransactions(
        runtime,
        ListTransactionsRequestInternal().apply {
          readMask?.let { this.readMask = it }
          filter?.let { this.filter = it }
          startCheckpoint = from.checkpoint
          if (from.transactionIndex > 0u) startTransactionIndex = from.transactionIndex
          if (until.transactionIndex == 0uL) {
            endCheckpoint = until.checkpoint
          } else {
            endCheckpoint = until.checkpoint + 1u
            endTransactionIndex = until.transactionIndex
          }
          from.cursor?.let { cursor -> options = QueryOptionsInternal().apply { after = cursor } }
        },
      )
    },
    fromScan = { frame ->
      val item = frame.transaction.takeIf { frame.presence.hasTransaction }
      val watermark = frame.watermark.takeIf { frame.presence.hasWatermark }
      Scanned(
        transactionAfter(watermark, item)?.let { Positioned(item, it) },
        frame.end.reason.takeIf { frame.presence.hasEnd },
      )
    },
    itemKey = { after ->
      if (after.transactionIndex > 0u) StreamPosition(after.checkpoint, after.transactionIndex - 1u)
      else after
    },
  )

// --- Events ------------------------------------------------------------------------------------

private fun eventAfter(watermark: Watermark?, item: Event?): StreamPosition? =
  watermark?.position()
    ?: item?.let { event ->
      val c = event.checkpoint ?: return@let null
      val t = event.transactionIndex ?: return@let null
      val e = event.eventIndex ?: return@let null
      StreamPosition(c, t, e + 1u, cursor = watermark?.cursor)
    }
    ?: watermark?.takeIf { item == null }?.coveredPosition()

internal fun eventSource(
  runtime: GrpcRuntime,
  filter: EventFilter?,
  readMask: FieldMask?,
): StreamSource<SubscribeEventsResponse, ListEventsResponse, Event> =
  StreamSource(
    subscribe = { subscribeEvents(runtime, filter, readMask) },
    fromLive = { frame ->
      val item = frame.event.takeIf { frame.presence.hasEvent }
      val watermark = frame.watermark.takeIf { frame.presence.hasWatermark }
      Positioned(item, eventAfter(watermark, item) ?: missingPosition("event"))
    },
    scan = { from, until ->
      listEvents(
        runtime,
        ListEventsRequestInternal().apply {
          readMask?.let { this.readMask = it }
          filter?.let { this.filter = it }
          startCheckpoint = from.checkpoint
          if (from.transactionIndex > 0u || from.eventIndex > 0u) {
            startTransactionIndex = from.transactionIndex
            startEventIndex = from.eventIndex
          }
          if (until.transactionIndex == 0uL && until.eventIndex == 0u) {
            endCheckpoint = until.checkpoint
          } else {
            endCheckpoint = until.checkpoint + 1u
            endTransactionIndex = until.transactionIndex
            endEventIndex = until.eventIndex
          }
          from.cursor?.let { cursor -> options = QueryOptionsInternal().apply { after = cursor } }
        },
      )
    },
    fromScan = { frame ->
      val item = frame.event.takeIf { frame.presence.hasEvent }
      val watermark = frame.watermark.takeIf { frame.presence.hasWatermark }
      Scanned(
        eventAfter(watermark, item)?.let { Positioned(item, it) },
        frame.end.reason.takeIf { frame.presence.hasEnd },
      )
    },
    itemKey = { after ->
      if (after.eventIndex > 0u) after.copy(eventIndex = after.eventIndex - 1u, cursor = null)
      else after.copy(cursor = null)
    },
  )
