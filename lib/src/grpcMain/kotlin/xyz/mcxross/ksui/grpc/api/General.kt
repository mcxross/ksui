package xyz.mcxross.ksui.grpc.api

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.GetCheckpointRequest
import sui.rpc.v2.GetCheckpointResponse
import sui.rpc.v2.GetEpochRequest
import sui.rpc.v2.GetEpochResponse
import sui.rpc.v2.GetServiceInfoResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.getCheckpoint as internalGetCheckpoint
import xyz.mcxross.ksui.grpc.internal.getEpoch as internalGetEpoch
import xyz.mcxross.ksui.grpc.internal.getServiceInfo as internalGetServiceInfo
import xyz.mcxross.ksui.grpc.protocol.General as GeneralProtocol
import xyz.mcxross.ksui.model.CheckpointId

internal class General(private val runtime: GrpcRuntime) : GeneralProtocol {
  override suspend fun getServiceInfo(): GetServiceInfoResponse = internalGetServiceInfo(runtime)

  override suspend fun getCheckpoint(request: GetCheckpointRequest): GetCheckpointResponse =
    internalGetCheckpoint(runtime, request)

  override suspend fun getCheckpoint(
    checkpointId: CheckpointId?,
    readMask: FieldMask?,
  ): GetCheckpointResponse = internalGetCheckpoint(runtime, checkpointId, readMask)

  override suspend fun getEpoch(request: GetEpochRequest): GetEpochResponse =
    internalGetEpoch(runtime, request)

  override suspend fun getEpoch(epoch: ULong?, readMask: FieldMask?): GetEpochResponse =
    internalGetEpoch(runtime, epoch, readMask)
}
