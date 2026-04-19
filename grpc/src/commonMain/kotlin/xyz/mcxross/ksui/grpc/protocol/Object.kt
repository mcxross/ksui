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
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

interface Object {
  suspend fun getObject(request: GetObjectRequest): Result<GetObjectResponse, SuiError>

  suspend fun getObject(
    objectId: String,
    version: ULong? = null,
    readMask: FieldMask? = null,
  ): Result<GetObjectResponse, SuiError>

  suspend fun batchGetObjects(
    requests: List<GetObjectRequest>,
    readMask: FieldMask? = null,
  ): Result<BatchGetObjectsResponse, SuiError>

  suspend fun listDynamicFields(
    request: ListDynamicFieldsRequest
  ): Result<ListDynamicFieldsResponse, SuiError>

  suspend fun listDynamicFields(
    parent: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
    readMask: FieldMask? = null,
  ): Result<ListDynamicFieldsResponse, SuiError>

  suspend fun listOwnedObjects(
    request: ListOwnedObjectsRequest
  ): Result<ListOwnedObjectsResponse, SuiError>

  suspend fun listOwnedObjects(
    owner: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
    readMask: FieldMask? = null,
    objectType: String? = null,
  ): Result<ListOwnedObjectsResponse, SuiError>

  suspend fun listOwnedObjects(
    owner: AccountAddress,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
    readMask: FieldMask? = null,
    objectType: String? = null,
  ): Result<ListOwnedObjectsResponse, SuiError>
}
