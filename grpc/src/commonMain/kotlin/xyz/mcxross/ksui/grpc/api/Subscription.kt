package xyz.mcxross.ksui.grpc.api

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.asGrpcResult
import xyz.mcxross.ksui.grpc.internal.subscribeCheckpoints as internalSubscribeCheckpoints
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
}
