package xyz.mcxross.ksui.grpc.internal

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsRequestInternal
import sui.rpc.v2.SubscribeCheckpointsResponse

internal fun subscribeCheckpoints(
  runtime: GrpcRuntime,
  request: SubscribeCheckpointsRequest,
): Flow<SubscribeCheckpointsResponse> = runtime.subscriptionService.SubscribeCheckpoints(request)

internal fun subscribeCheckpoints(
  runtime: GrpcRuntime,
  readMask: FieldMask? = null,
): Flow<SubscribeCheckpointsResponse> =
  subscribeCheckpoints(
    runtime,
    SubscribeCheckpointsRequestInternal().apply { readMask?.let { this.readMask = it } },
  )
