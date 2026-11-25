package xyz.mcxross.ksui.grpc

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.io.bytestring.ByteString

/**
 * A boundary in ledger order: every matching item before it has been delivered.
 *
 * Positions compare by [checkpoint], then [transactionIndex], then [eventIndex]; [cursor] does not
 * take part. A checkpoint stream only uses [checkpoint]. Save the position of the last frame you
 * handled and pass it back as `resumeFrom` to continue after a restart.
 *
 * @property checkpoint checkpoint sequence number.
 * @property transactionIndex zero-based transaction index within [checkpoint].
 * @property eventIndex zero-based event index within the transaction (event streams only).
 * @property cursor the server's opaque cursor for this point, when it sent one. Pass it back as is;
 *   never inspect or compare it.
 */
data class StreamPosition(
  val checkpoint: ULong,
  val transactionIndex: ULong = 0u,
  val eventIndex: UInt = 0u,
  val cursor: ByteString? = null,
) : Comparable<StreamPosition> {
  override fun compareTo(other: StreamPosition): Int =
    compareValuesBy(this, other, { it.checkpoint }, { it.transactionIndex }, { it.eventIndex })

  companion object {
    /** The start of [checkpoint], to begin a stream there. */
    fun atCheckpoint(checkpoint: ULong): StreamPosition = StreamPosition(checkpoint)
  }
}

/**
 * One frame of a resumable stream.
 *
 * @property item the matching checkpoint, transaction or event, or `null` when the frame only moves
 *   [resumeFrom] past items that did not match the filter.
 * @property resumeFrom where to resume to continue right after this frame.
 * @property replayed `true` when the frame came from replaying a gap with a List scan, after a
 *   reconnect or when starting from an earlier position, rather than from the live subscription.
 */
data class StreamFrame<out T : Any>(
  val item: T?,
  val resumeFrom: StreamPosition,
  val replayed: Boolean,
)

/**
 * How a resumable stream reconnects after the server closes it.
 *
 * It retries on transient gRPC statuses (`UNAVAILABLE`, `INTERNAL`, `UNKNOWN`, `ABORTED`,
 * `RESOURCE_EXHAUSTED`, `CANCELLED`, `DEADLINE_EXCEEDED`) and when the server ends a stream, which
 * subscriptions never do successfully. Other statuses, such as `INVALID_ARGUMENT` for a bad filter
 * or `NOT_FOUND` for a pruned resume point, end the stream with an error.
 *
 * @property maxAttempts consecutive failed attempts before giving up. Any delivered frame resets
 *   the count.
 * @property initialDelay delay before the first retry; it doubles each attempt up to [maxDelay].
 * @property ledgerTipPollInterval how often a replay polls while the List index trails the live
 *   stream (the scan ends with `LEDGER_TIP`).
 */
data class StreamRetryPolicy(
  val maxAttempts: Int = Int.MAX_VALUE,
  val initialDelay: Duration = 500.milliseconds,
  val maxDelay: Duration = 30.seconds,
  val ledgerTipPollInterval: Duration = 500.milliseconds,
) {
  init {
    require(maxAttempts >= 0) { "maxAttempts must not be negative" }
    require(initialDelay >= Duration.ZERO && maxDelay >= initialDelay) {
      "Delays must satisfy 0 <= initialDelay <= maxDelay"
    }
  }

  companion object {
    /** Fail on the first error instead of reconnecting. */
    val NONE: StreamRetryPolicy = StreamRetryPolicy(maxAttempts = 0)
  }
}
