package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.EventFilter
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

/**
 * Streaming access to the ledger: live subscriptions and the List scans they pair with.
 *
 * A subscription starts at the server's latest executed checkpoint and cannot resume from an
 * earlier point. Every frame carries a resume point: a checkpoint `cursor` for checkpoint streams,
 * and a `Watermark` for transaction and event streams. To fill a gap after a disconnect, replay it
 * with the List scan of the same name from the last resume point.
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
}
