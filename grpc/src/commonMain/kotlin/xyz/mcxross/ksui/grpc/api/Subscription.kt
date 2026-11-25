package xyz.mcxross.ksui.grpc.api

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
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.asGrpcResult
import xyz.mcxross.ksui.grpc.internal.listCheckpoints as internalListCheckpoints
import xyz.mcxross.ksui.grpc.internal.listEvents as internalListEvents
import xyz.mcxross.ksui.grpc.internal.listTransactions as internalListTransactions
import xyz.mcxross.ksui.grpc.internal.subscribeCheckpoints as internalSubscribeCheckpoints
import xyz.mcxross.ksui.grpc.internal.subscribeEvents as internalSubscribeEvents
import xyz.mcxross.ksui.grpc.internal.subscribeTransactions as internalSubscribeTransactions
import xyz.mcxross.ksui.grpc.protocol.Subscription as SubscriptionProtocol

internal class Subscription(private val runtime: GrpcRuntime) : SubscriptionProtocol {
  override fun subscribeCheckpoints(
    request: SubscribeCheckpointsRequest
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>> =
    internalSubscribeCheckpoints(runtime, request).asGrpcResult()

  override fun subscribeCheckpoints(
    readMask: FieldMask?
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>> =
    internalSubscribeCheckpoints(runtime, readMask).asGrpcResult()

  override fun subscribeCheckpoints(
    filter: TransactionFilter,
    readMask: FieldMask?,
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>> =
    internalSubscribeCheckpoints(runtime, readMask, filter).asGrpcResult()

  override fun subscribeTransactions(
    request: SubscribeTransactionsRequest
  ): Flow<Result<SubscribeTransactionsResponse, SuiError>> =
    internalSubscribeTransactions(runtime, request).asGrpcResult()

  override fun subscribeTransactions(
    filter: TransactionFilter?,
    readMask: FieldMask?,
  ): Flow<Result<SubscribeTransactionsResponse, SuiError>> =
    internalSubscribeTransactions(runtime, filter, readMask).asGrpcResult()

  override fun subscribeEvents(
    request: SubscribeEventsRequest
  ): Flow<Result<SubscribeEventsResponse, SuiError>> =
    internalSubscribeEvents(runtime, request).asGrpcResult()

  override fun subscribeEvents(
    filter: EventFilter?,
    readMask: FieldMask?,
  ): Flow<Result<SubscribeEventsResponse, SuiError>> =
    internalSubscribeEvents(runtime, filter, readMask).asGrpcResult()

  override fun listCheckpoints(
    request: ListCheckpointsRequest
  ): Flow<Result<ListCheckpointsResponse, SuiError>> =
    internalListCheckpoints(runtime, request).asGrpcResult()

  override fun listTransactions(
    request: ListTransactionsRequest
  ): Flow<Result<ListTransactionsResponse, SuiError>> =
    internalListTransactions(runtime, request).asGrpcResult()

  override fun listEvents(request: ListEventsRequest): Flow<Result<ListEventsResponse, SuiError>> =
    internalListEvents(runtime, request).asGrpcResult()
}
