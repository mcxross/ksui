package xyz.mcxross.ksui.grpc.api

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetDatatypeRequest
import sui.rpc.v2.GetDatatypeResponse
import sui.rpc.v2.GetFunctionRequest
import sui.rpc.v2.GetFunctionResponse
import sui.rpc.v2.GetPackageRequest
import sui.rpc.v2.GetPackageResponse
import sui.rpc.v2.ListPackageVersionsRequest
import sui.rpc.v2.ListPackageVersionsResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.handleGrpc
import xyz.mcxross.ksui.grpc.internal.getDatatype as internalGetDatatype
import xyz.mcxross.ksui.grpc.internal.getFunction as internalGetFunction
import xyz.mcxross.ksui.grpc.internal.getPackage as internalGetPackage
import xyz.mcxross.ksui.grpc.internal.listPackageVersions as internalListPackageVersions
import xyz.mcxross.ksui.grpc.protocol.Move as MoveProtocol
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.Result

internal class Move(private val runtime: GrpcRuntime) : MoveProtocol {
  override suspend fun getPackage(
    request: GetPackageRequest
  ): Result<GetPackageResponse, SuiError> =
    handleGrpc { internalGetPackage(runtime, request) }

  override suspend fun getPackage(packageId: String): Result<GetPackageResponse, SuiError> =
    handleGrpc { internalGetPackage(runtime, packageId) }

  override suspend fun getDatatype(
    request: GetDatatypeRequest
  ): Result<GetDatatypeResponse, SuiError> = handleGrpc { internalGetDatatype(runtime, request) }

  override suspend fun getDatatype(
    packageId: String,
    moduleName: String,
    name: String,
  ): Result<GetDatatypeResponse, SuiError> =
    handleGrpc { internalGetDatatype(runtime, packageId, moduleName, name) }

  override suspend fun getFunction(
    request: GetFunctionRequest
  ): Result<GetFunctionResponse, SuiError> = handleGrpc { internalGetFunction(runtime, request) }

  override suspend fun getFunction(
    packageId: String,
    moduleName: String,
    name: String,
  ): Result<GetFunctionResponse, SuiError> =
    handleGrpc { internalGetFunction(runtime, packageId, moduleName, name) }

  override suspend fun listPackageVersions(
    request: ListPackageVersionsRequest
  ): Result<ListPackageVersionsResponse, SuiError> =
    handleGrpc { internalListPackageVersions(runtime, request) }

  override suspend fun listPackageVersions(
    packageId: String,
    pageSize: UInt?,
    pageToken: ByteString?,
  ): Result<ListPackageVersionsResponse, SuiError> =
    handleGrpc { internalListPackageVersions(runtime, packageId, pageSize, pageToken) }
}
