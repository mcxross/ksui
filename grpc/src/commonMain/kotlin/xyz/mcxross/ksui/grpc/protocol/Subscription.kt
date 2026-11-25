package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.Checkpoint
import sui.rpc.v2.Event
import sui.rpc.v2.EventFilter
import sui.rpc.v2.ExecutedTransaction
import sui.rpc.v2.ListCheckpointsRequest
import sui.rpc.v2.ListCheckpointsResponse
import sui.rpc.v2.ListEventsRequest
import sui.rpc.v2.ListEventsResponse
import sui.rpc.v2.ListTransactionsRequest
import sui.rpc.v2.ListTransactionsResponse
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse
import sui.rpc.v2.SubscribeEventsRequest
import sui.rpc.v2.SubscribeEventsResponse
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.SubscribeTransactionsResponse
import sui.rpc.v2.TransactionFilter
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.grpc.StreamFrame
import xyz.mcxross.ksui.grpc.StreamPosition
import xyz.mcxross.ksui.grpc.StreamRetryPolicy

/**
 * Streaming access to the ledger: live subscriptions and the List scans they pair with.
 *
 * A subscription starts at the server's latest executed checkpoint and cannot resume from an
 * earlier point. Every frame carries a resume point: a checkpoint `cursor` for checkpoint streams,
 * and a `Watermark` for transaction and event streams. To fill a gap after a disconnect, replay it
 * with the List scan of the same name from the last resume point. [checkpointStream],
 * [transactionStream] and [eventStream] do this for you.
 *
 * Each flow ends with at most one [Result.Err], when the server closes the stream with a gRPC
 * status.
 */
interface Subscription {
  fun subscribeCheckpoints(
    request: SubscribeCheckpointsRequest
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>>

  fun subscribeCheckpoints(
    readMask: FieldMask? = null
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>>

  /** Checkpoints that contain at least one transaction matching [filter]. */
  fun subscribeCheckpoints(
    filter: TransactionFilter,
    readMask: FieldMask? = null,
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>>

  fun subscribeTransactions(
    request: SubscribeTransactionsRequest
  ): Flow<Result<SubscribeTransactionsResponse, SuiError>>

  /** Transactions as they execute. A filtered stream opens with a progress-only frame. */
  fun subscribeTransactions(
    filter: TransactionFilter? = null,
    readMask: FieldMask? = null,
  ): Flow<Result<SubscribeTransactionsResponse, SuiError>>

  fun subscribeEvents(
    request: SubscribeEventsRequest
  ): Flow<Result<SubscribeEventsResponse, SuiError>>

  /** Events as they are emitted. A filtered stream opens with a progress-only frame. */
  fun subscribeEvents(
    filter: EventFilter? = null,
    readMask: FieldMask? = null,
  ): Flow<Result<SubscribeEventsResponse, SuiError>>

  /**
   * Scans checkpoints in `[start_checkpoint, end_checkpoint)`. The last frame carries a `QueryEnd`
   * that says why the scan stopped; resume an item- or scan-limited scan from its watermark.
   */
  fun listCheckpoints(
    request: ListCheckpointsRequest
  ): Flow<Result<ListCheckpointsResponse, SuiError>>

  /** Scans transactions; see [listCheckpoints] for how a scan ends and resumes. */
  fun listTransactions(
    request: ListTransactionsRequest
  ): Flow<Result<ListTransactionsResponse, SuiError>>

  /** Scans events; see [listCheckpoints] for how a scan ends and resumes. */
  fun listEvents(request: ListEventsRequest): Flow<Result<ListEventsResponse, SuiError>>

  /**
   * Checkpoints from [resumeFrom], with no gaps or duplicates across reconnects.
   *
   * With [resumeFrom] set, the stream first replays what it missed with a List scan, then
   * continues live; `null` starts at the live tip. Save [StreamFrame.resumeFrom] of the last frame
   * you handled to continue there later, for example after a restart. With a [filter], frames
   * without an item keep the resume point moving while nothing matches.
   *
   * The flow reconnects according to [retry] and ends only with a [Result.Err], when retrying
   * stops, or when the collector cancels it.
   */
  fun checkpointStream(
    resumeFrom: StreamPosition? = null,
    filter: TransactionFilter? = null,
    readMask: FieldMask? = null,
    retry: StreamRetryPolicy = StreamRetryPolicy(),
  ): Flow<Result<StreamFrame<Checkpoint>, SuiError>>

  /** Transactions from [resumeFrom], like [checkpointStream]. */
  fun transactionStream(
    resumeFrom: StreamPosition? = null,
    filter: TransactionFilter? = null,
    readMask: FieldMask? = null,
    retry: StreamRetryPolicy = StreamRetryPolicy(),
  ): Flow<Result<StreamFrame<ExecutedTransaction>, SuiError>>

  /** Events from [resumeFrom], like [checkpointStream]. */
  fun eventStream(
    resumeFrom: StreamPosition? = null,
    filter: EventFilter? = null,
    readMask: FieldMask? = null,
    retry: StreamRetryPolicy = StreamRetryPolicy(),
  ): Flow<Result<StreamFrame<Event>, SuiError>>
}
