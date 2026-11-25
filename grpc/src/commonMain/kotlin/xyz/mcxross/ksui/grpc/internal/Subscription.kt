package xyz.mcxross.ksui.grpc.internal

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
import sui.rpc.v2.SubscribeCheckpointsRequestInternal
import sui.rpc.v2.SubscribeCheckpointsResponse
import sui.rpc.v2.SubscribeEventsRequest
import sui.rpc.v2.SubscribeEventsRequestInternal
import sui.rpc.v2.SubscribeEventsResponse
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.SubscribeTransactionsRequestInternal
import sui.rpc.v2.SubscribeTransactionsResponse
import sui.rpc.v2.TransactionFilter

internal fun subscribeCheckpoints(
  runtime: GrpcRuntime,
  request: SubscribeCheckpointsRequest,
): Flow<SubscribeCheckpointsResponse> = runtime.subscriptionService.SubscribeCheckpoints(request)

internal fun subscribeCheckpoints(
  runtime: GrpcRuntime,
  readMask: FieldMask? = null,
  filter: TransactionFilter? = null,
): Flow<SubscribeCheckpointsResponse> =
  subscribeCheckpoints(
    runtime,
    SubscribeCheckpointsRequestInternal().apply {
      readMask?.let { this.readMask = it }
      filter?.let { this.filter = it }
    },
  )

internal fun subscribeTransactions(
  runtime: GrpcRuntime,
  request: SubscribeTransactionsRequest,
): Flow<SubscribeTransactionsResponse> =
  runtime.subscriptionService.SubscribeTransactions(request)

internal fun subscribeTransactions(
  runtime: GrpcRuntime,
  filter: TransactionFilter? = null,
  readMask: FieldMask? = null,
): Flow<SubscribeTransactionsResponse> =
  subscribeTransactions(
    runtime,
    SubscribeTransactionsRequestInternal().apply {
      readMask?.let { this.readMask = it }
      filter?.let { this.filter = it }
    },
  )

internal fun subscribeEvents(
  runtime: GrpcRuntime,
  request: SubscribeEventsRequest,
): Flow<SubscribeEventsResponse> = runtime.subscriptionService.SubscribeEvents(request)

internal fun subscribeEvents(
  runtime: GrpcRuntime,
  filter: EventFilter? = null,
  readMask: FieldMask? = null,
): Flow<SubscribeEventsResponse> =
  subscribeEvents(
    runtime,
    SubscribeEventsRequestInternal().apply {
      readMask?.let { this.readMask = it }
      filter?.let { this.filter = it }
    },
  )

internal fun listCheckpoints(
  runtime: GrpcRuntime,
  request: ListCheckpointsRequest,
): Flow<ListCheckpointsResponse> = runtime.ledgerService.ListCheckpoints(request)

internal fun listTransactions(
  runtime: GrpcRuntime,
  request: ListTransactionsRequest,
): Flow<ListTransactionsResponse> = runtime.ledgerService.ListTransactions(request)

internal fun listEvents(
  runtime: GrpcRuntime,
  request: ListEventsRequest,
): Flow<ListEventsResponse> = runtime.ledgerService.ListEvents(request)
