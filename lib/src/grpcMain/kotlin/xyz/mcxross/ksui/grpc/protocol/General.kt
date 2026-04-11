package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.GetCheckpointRequest
import sui.rpc.v2.GetCheckpointResponse
import sui.rpc.v2.GetEpochRequest
import sui.rpc.v2.GetEpochResponse
import sui.rpc.v2.GetServiceInfoResponse
import xyz.mcxross.ksui.model.CheckpointId

interface General {
  suspend fun getServiceInfo(): GetServiceInfoResponse

  suspend fun getCheckpoint(request: GetCheckpointRequest): GetCheckpointResponse

  suspend fun getCheckpoint(
    checkpointId: CheckpointId? = null,
    readMask: FieldMask? = null,
  ): GetCheckpointResponse

  suspend fun getEpoch(request: GetEpochRequest): GetEpochResponse

  suspend fun getEpoch(epoch: ULong? = null, readMask: FieldMask? = null): GetEpochResponse
}
