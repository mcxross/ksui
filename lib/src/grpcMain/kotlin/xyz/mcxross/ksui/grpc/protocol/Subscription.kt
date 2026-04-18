package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse

interface Subscription {
  fun subscribeCheckpoints(request: SubscribeCheckpointsRequest): Flow<SubscribeCheckpointsResponse>

  fun subscribeCheckpoints(readMask: FieldMask? = null): Flow<SubscribeCheckpointsResponse>
}
