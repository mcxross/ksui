package xyz.mcxross.ksui.grpc.api

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.GetCheckpointRequest
import sui.rpc.v2.GetCheckpointResponse
import sui.rpc.v2.GetEpochRequest
import sui.rpc.v2.GetEpochResponse
import sui.rpc.v2.GetServiceInfoResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.handleGrpc
import xyz.mcxross.ksui.grpc.internal.getCheckpoint as internalGetCheckpoint
import xyz.mcxross.ksui.grpc.internal.getEpoch as internalGetEpoch
import xyz.mcxross.ksui.grpc.internal.getServiceInfo as internalGetServiceInfo
import xyz.mcxross.ksui.grpc.protocol.General as GeneralProtocol
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Result

internal class General(private val runtime: GrpcRuntime) : GeneralProtocol {
  override suspend fun getServiceInfo(): Result<GetServiceInfoResponse, SuiError> =
    handleGrpc { internalGetServiceInfo(runtime) }

  override suspend fun getCheckpoint(
    request: GetCheckpointRequest
  ): Result<GetCheckpointResponse, SuiError> =
    handleGrpc { internalGetCheckpoint(runtime, request) }

  override suspend fun getCheckpoint(
    checkpointId: CheckpointId?,
    readMask: FieldMask?,
  ): Result<GetCheckpointResponse, SuiError> =
    handleGrpc { internalGetCheckpoint(runtime, checkpointId, readMask) }

  override suspend fun getEpoch(request: GetEpochRequest): Result<GetEpochResponse, SuiError> =
    handleGrpc { internalGetEpoch(runtime, request) }

  override suspend fun getEpoch(
    epoch: ULong?,
    readMask: FieldMask?,
  ): Result<GetEpochResponse, SuiError> = handleGrpc { internalGetEpoch(runtime, epoch, readMask) }
}
