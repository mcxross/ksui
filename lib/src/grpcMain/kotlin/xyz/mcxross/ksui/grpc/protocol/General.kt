package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.GetCheckpointRequest
import sui.rpc.v2.GetCheckpointResponse
import sui.rpc.v2.GetEpochRequest
import sui.rpc.v2.GetEpochResponse
import sui.rpc.v2.GetServiceInfoResponse
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Result

interface General {
  suspend fun getServiceInfo(): Result<GetServiceInfoResponse, SuiError>

  suspend fun getCheckpoint(request: GetCheckpointRequest): Result<GetCheckpointResponse, SuiError>

  suspend fun getCheckpoint(
    checkpointId: CheckpointId? = null,
    readMask: FieldMask? = null,
  ): Result<GetCheckpointResponse, SuiError>

  suspend fun getEpoch(request: GetEpochRequest): Result<GetEpochResponse, SuiError>

  suspend fun getEpoch(
    epoch: ULong? = null,
    readMask: FieldMask? = null,
  ): Result<GetEpochResponse, SuiError>
}
