package xyz.mcxross.ksui.grpc.api

import com.google.protobuf.kotlin.FieldMask
import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.BatchGetObjectsResponse
import sui.rpc.v2.GetObjectRequest
import sui.rpc.v2.GetObjectResponse
import sui.rpc.v2.ListDynamicFieldsRequest
import sui.rpc.v2.ListDynamicFieldsResponse
import sui.rpc.v2.ListOwnedObjectsRequest
import sui.rpc.v2.ListOwnedObjectsResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.batchGetObjects as internalBatchGetObjects
import xyz.mcxross.ksui.grpc.internal.getObject as internalGetObject
import xyz.mcxross.ksui.grpc.internal.listDynamicFields as internalListDynamicFields
import xyz.mcxross.ksui.grpc.internal.listOwnedObjects as internalListOwnedObjects
import xyz.mcxross.ksui.grpc.protocol.Object as ObjectProtocol
import xyz.mcxross.ksui.model.AccountAddress

internal class Object(private val runtime: GrpcRuntime) : ObjectProtocol {
  override suspend fun getObject(request: GetObjectRequest): GetObjectResponse =
    internalGetObject(runtime, request)

  override suspend fun getObject(
    objectId: String,
    version: ULong?,
    readMask: FieldMask?,
  ): GetObjectResponse = internalGetObject(runtime, objectId, version, readMask)

  override suspend fun batchGetObjects(
    requests: List<GetObjectRequest>,
    readMask: FieldMask?,
  ): BatchGetObjectsResponse = internalBatchGetObjects(runtime, requests, readMask)

  override suspend fun listDynamicFields(
    request: ListDynamicFieldsRequest
  ): ListDynamicFieldsResponse = internalListDynamicFields(runtime, request)

  override suspend fun listDynamicFields(
    parent: String,
    pageSize: UInt?,
    pageToken: ByteString?,
    readMask: FieldMask?,
  ): ListDynamicFieldsResponse =
    internalListDynamicFields(runtime, parent, pageSize, pageToken, readMask)

  override suspend fun listOwnedObjects(
    request: ListOwnedObjectsRequest
  ): ListOwnedObjectsResponse = internalListOwnedObjects(runtime, request)

  override suspend fun listOwnedObjects(
    owner: String,
    pageSize: UInt?,
    pageToken: ByteString?,
    readMask: FieldMask?,
    objectType: String?,
  ): ListOwnedObjectsResponse =
    internalListOwnedObjects(runtime, owner, pageSize, pageToken, readMask, objectType)

  override suspend fun listOwnedObjects(
    owner: AccountAddress,
    pageSize: UInt?,
    pageToken: ByteString?,
    readMask: FieldMask?,
    objectType: String?,
  ): ListOwnedObjectsResponse =
    internalListOwnedObjects(runtime, owner, pageSize, pageToken, readMask, objectType)
}
