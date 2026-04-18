package xyz.mcxross.ksui.grpc.protocol

import com.google.protobuf.kotlin.FieldMask
import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.BatchGetObjectsResponse
import sui.rpc.v2.GetObjectRequest
import sui.rpc.v2.GetObjectResponse
import sui.rpc.v2.ListDynamicFieldsRequest
import sui.rpc.v2.ListDynamicFieldsResponse
import sui.rpc.v2.ListOwnedObjectsRequest
import sui.rpc.v2.ListOwnedObjectsResponse
import xyz.mcxross.ksui.model.AccountAddress

interface Object {
  suspend fun getObject(request: GetObjectRequest): GetObjectResponse

  suspend fun getObject(
    objectId: String,
    version: ULong? = null,
    readMask: FieldMask? = null,
  ): GetObjectResponse

  suspend fun batchGetObjects(
    requests: List<GetObjectRequest>,
    readMask: FieldMask? = null,
  ): BatchGetObjectsResponse

  suspend fun listDynamicFields(request: ListDynamicFieldsRequest): ListDynamicFieldsResponse

  suspend fun listDynamicFields(
    parent: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
    readMask: FieldMask? = null,
  ): ListDynamicFieldsResponse

  suspend fun listOwnedObjects(request: ListOwnedObjectsRequest): ListOwnedObjectsResponse

  suspend fun listOwnedObjects(
    owner: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
    readMask: FieldMask? = null,
    objectType: String? = null,
  ): ListOwnedObjectsResponse

  suspend fun listOwnedObjects(
    owner: AccountAddress,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
    readMask: FieldMask? = null,
    objectType: String? = null,
  ): ListOwnedObjectsResponse
}
