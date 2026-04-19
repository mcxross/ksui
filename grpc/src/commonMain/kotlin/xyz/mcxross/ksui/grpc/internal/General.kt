package xyz.mcxross.ksui.grpc.internal

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.GetCheckpointRequest
import sui.rpc.v2.GetCheckpointRequestInternal
import sui.rpc.v2.GetCheckpointResponse
import sui.rpc.v2.GetEpochRequest
import sui.rpc.v2.GetEpochRequestInternal
import sui.rpc.v2.GetEpochResponse
import sui.rpc.v2.GetServiceInfoRequestInternal
import sui.rpc.v2.GetServiceInfoResponse
import xyz.mcxross.ksui.model.CheckpointId as SuiCheckpointId

internal suspend fun getServiceInfo(runtime: GrpcRuntime): GetServiceInfoResponse =
  runtime.ledgerService.GetServiceInfo(GetServiceInfoRequestInternal())

internal suspend fun getCheckpoint(
  runtime: GrpcRuntime,
  request: GetCheckpointRequest,
): GetCheckpointResponse = runtime.ledgerService.GetCheckpoint(request)

internal suspend fun getCheckpoint(
  runtime: GrpcRuntime,
  checkpointId: SuiCheckpointId? = null,
  readMask: FieldMask? = null,
): GetCheckpointResponse {
  val sequenceNumber = checkpointId?.sequenceNumber
  require(sequenceNumber == null || sequenceNumber >= 0) {
    "Checkpoint sequenceNumber must be non-negative"
  }
  require(checkpointId?.digest == null || sequenceNumber == null) {
    "CheckpointId must set either digest or sequenceNumber, not both"
  }

  return getCheckpoint(
    runtime,
    GetCheckpointRequestInternal().apply {
      sequenceNumber?.let {
        this.checkpointId = GetCheckpointRequest.CheckpointId.SequenceNumber(it.toULong())
      }
      checkpointId?.digest?.let { this.checkpointId = GetCheckpointRequest.CheckpointId.Digest(it) }
      readMask?.let { this.readMask = it }
    },
  )
}

internal suspend fun getEpoch(runtime: GrpcRuntime, request: GetEpochRequest): GetEpochResponse =
  runtime.ledgerService.GetEpoch(request)

internal suspend fun getEpoch(
  runtime: GrpcRuntime,
  epoch: ULong? = null,
  readMask: FieldMask? = null,
): GetEpochResponse =
  getEpoch(
    runtime,
    GetEpochRequestInternal().apply {
      epoch?.let { this.epoch = it }
      readMask?.let { this.readMask = it }
    },
  )
