package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import kotlinx.coroutines.flow.Flow
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result

interface Subscription {
  fun subscribeCheckpoints(
    request: SubscribeCheckpointsRequest
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>>

  fun subscribeCheckpoints(
    readMask: FieldMask? = null
  ): Flow<Result<SubscribeCheckpointsResponse, SuiError>>
}
