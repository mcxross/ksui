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
import xyz.mcxross.ksui.grpc.internal.getDatatype as internalGetDatatype
import xyz.mcxross.ksui.grpc.internal.getFunction as internalGetFunction
import xyz.mcxross.ksui.grpc.internal.getPackage as internalGetPackage
import xyz.mcxross.ksui.grpc.internal.listPackageVersions as internalListPackageVersions
import xyz.mcxross.ksui.grpc.protocol.Move as MoveProtocol

internal class Move(private val runtime: GrpcRuntime) : MoveProtocol {
  override suspend fun getPackage(request: GetPackageRequest): GetPackageResponse =
    internalGetPackage(runtime, request)

  override suspend fun getPackage(packageId: String): GetPackageResponse =
    internalGetPackage(runtime, packageId)

  override suspend fun getDatatype(request: GetDatatypeRequest): GetDatatypeResponse =
    internalGetDatatype(runtime, request)

  override suspend fun getDatatype(
    packageId: String,
    moduleName: String,
    name: String,
  ): GetDatatypeResponse = internalGetDatatype(runtime, packageId, moduleName, name)

  override suspend fun getFunction(request: GetFunctionRequest): GetFunctionResponse =
    internalGetFunction(runtime, request)

  override suspend fun getFunction(
    packageId: String,
    moduleName: String,
    name: String,
  ): GetFunctionResponse = internalGetFunction(runtime, packageId, moduleName, name)

  override suspend fun listPackageVersions(
    request: ListPackageVersionsRequest
  ): ListPackageVersionsResponse = internalListPackageVersions(runtime, request)

  override suspend fun listPackageVersions(
    packageId: String,
    pageSize: UInt?,
    pageToken: ByteString?,
  ): ListPackageVersionsResponse =
    internalListPackageVersions(runtime, packageId, pageSize, pageToken)
}
