package xyz.mcxross.ksui.grpc.internal

import com.google.protobuf.kotlin.FieldMask
import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.BatchGetObjectsRequestInternal
import sui.rpc.v2.BatchGetObjectsResponse
import sui.rpc.v2.GetObjectRequest
import sui.rpc.v2.GetObjectRequestInternal
import sui.rpc.v2.GetObjectResponse
import sui.rpc.v2.ListDynamicFieldsRequest
import sui.rpc.v2.ListDynamicFieldsRequestInternal
import sui.rpc.v2.ListDynamicFieldsResponse
import sui.rpc.v2.ListOwnedObjectsRequest
import sui.rpc.v2.ListOwnedObjectsRequestInternal
import sui.rpc.v2.ListOwnedObjectsResponse
import xyz.mcxross.ksui.model.AccountAddress

internal suspend fun getObject(runtime: GrpcRuntime, request: GetObjectRequest): GetObjectResponse =
  runtime.ledgerService.GetObject(request)

internal suspend fun getObject(
  runtime: GrpcRuntime,
  objectId: String,
  version: ULong? = null,
  readMask: FieldMask? = null,
): GetObjectResponse =
  getObject(
    runtime,
    GetObjectRequestInternal().apply {
      this.objectId = objectId
      version?.let { this.version = it }
      readMask?.let { this.readMask = it }
    },
  )

internal suspend fun batchGetObjects(
  runtime: GrpcRuntime,
  requests: List<GetObjectRequest>,
  readMask: FieldMask? = null,
): BatchGetObjectsResponse =
  runtime.ledgerService.BatchGetObjects(
    BatchGetObjectsRequestInternal().apply {
      this.requests = requests
      readMask?.let { this.readMask = it }
    }
  )

internal suspend fun listDynamicFields(
  runtime: GrpcRuntime,
  request: ListDynamicFieldsRequest,
): ListDynamicFieldsResponse = runtime.stateService.ListDynamicFields(request)

internal suspend fun listDynamicFields(
  runtime: GrpcRuntime,
  parent: String,
  pageSize: UInt? = null,
  pageToken: ByteString? = null,
  readMask: FieldMask? = null,
): ListDynamicFieldsResponse =
  listDynamicFields(
    runtime,
    ListDynamicFieldsRequestInternal().apply {
      this.parent = parent
      pageSize?.let { this.pageSize = it }
      pageToken?.let { this.pageToken = it }
      readMask?.let { this.readMask = it }
    },
  )

internal suspend fun listOwnedObjects(
  runtime: GrpcRuntime,
  request: ListOwnedObjectsRequest,
): ListOwnedObjectsResponse = runtime.stateService.ListOwnedObjects(request)

internal suspend fun listOwnedObjects(
  runtime: GrpcRuntime,
  owner: String,
  pageSize: UInt? = null,
  pageToken: ByteString? = null,
  readMask: FieldMask? = null,
  objectType: String? = null,
): ListOwnedObjectsResponse =
  listOwnedObjects(
    runtime,
    ListOwnedObjectsRequestInternal().apply {
      this.owner = owner
      pageSize?.let { this.pageSize = it }
      pageToken?.let { this.pageToken = it }
      readMask?.let { this.readMask = it }
      objectType?.let { this.objectType = it }
    },
  )

internal suspend fun listOwnedObjects(
  runtime: GrpcRuntime,
  owner: AccountAddress,
  pageSize: UInt? = null,
  pageToken: ByteString? = null,
  readMask: FieldMask? = null,
  objectType: String? = null,
): ListOwnedObjectsResponse =
  listOwnedObjects(runtime, owner.toString(), pageSize, pageToken, readMask, objectType)
