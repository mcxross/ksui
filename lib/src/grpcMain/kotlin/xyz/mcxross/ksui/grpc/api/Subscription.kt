package xyz.mcxross.ksui.grpc.api

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.subscribeCheckpoints as internalSubscribeCheckpoints
import xyz.mcxross.ksui.grpc.protocol.Subscription as SubscriptionProtocol

internal class Subscription(private val runtime: GrpcRuntime) : SubscriptionProtocol {
  override fun subscribeCheckpoints(
    request: SubscribeCheckpointsRequest
  ): Flow<SubscribeCheckpointsResponse> = internalSubscribeCheckpoints(runtime, request)

  override fun subscribeCheckpoints(readMask: FieldMask?): Flow<SubscribeCheckpointsResponse> =
    internalSubscribeCheckpoints(runtime, readMask)
}
