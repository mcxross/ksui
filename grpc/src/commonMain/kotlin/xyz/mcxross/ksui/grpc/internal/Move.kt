package xyz.mcxross.ksui.grpc.internal

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetDatatypeRequest
import sui.rpc.v2.GetDatatypeRequestInternal
import sui.rpc.v2.GetDatatypeResponse
import sui.rpc.v2.GetFunctionRequest
import sui.rpc.v2.GetFunctionRequestInternal
import sui.rpc.v2.GetFunctionResponse
import sui.rpc.v2.GetPackageRequest
import sui.rpc.v2.GetPackageRequestInternal
import sui.rpc.v2.GetPackageResponse
import sui.rpc.v2.ListPackageVersionsRequest
import sui.rpc.v2.ListPackageVersionsRequestInternal
import sui.rpc.v2.ListPackageVersionsResponse

internal suspend fun getPackage(
  runtime: GrpcRuntime,
  request: GetPackageRequest,
): GetPackageResponse = runtime.movePackageService.GetPackage(request)

internal suspend fun getPackage(runtime: GrpcRuntime, packageId: String): GetPackageResponse =
  getPackage(runtime, GetPackageRequestInternal().apply { this.packageId = packageId })

internal suspend fun getDatatype(
  runtime: GrpcRuntime,
  request: GetDatatypeRequest,
): GetDatatypeResponse = runtime.movePackageService.GetDatatype(request)

internal suspend fun getDatatype(
  runtime: GrpcRuntime,
  packageId: String,
  moduleName: String,
  name: String,
): GetDatatypeResponse =
  getDatatype(
    runtime,
    GetDatatypeRequestInternal().apply {
      this.packageId = packageId
      this.moduleName = moduleName
      this.name = name
    },
  )

internal suspend fun getFunction(
  runtime: GrpcRuntime,
  request: GetFunctionRequest,
): GetFunctionResponse = runtime.movePackageService.GetFunction(request)

internal suspend fun getFunction(
  runtime: GrpcRuntime,
  packageId: String,
  moduleName: String,
  name: String,
): GetFunctionResponse =
  getFunction(
    runtime,
    GetFunctionRequestInternal().apply {
      this.packageId = packageId
      this.moduleName = moduleName
      this.name = name
    },
  )

internal suspend fun listPackageVersions(
  runtime: GrpcRuntime,
  request: ListPackageVersionsRequest,
): ListPackageVersionsResponse = runtime.movePackageService.ListPackageVersions(request)

internal suspend fun listPackageVersions(
  runtime: GrpcRuntime,
  packageId: String,
  pageSize: UInt? = null,
  pageToken: ByteString? = null,
): ListPackageVersionsResponse =
  listPackageVersions(
    runtime,
    ListPackageVersionsRequestInternal().apply {
      this.packageId = packageId
      pageSize?.let { this.pageSize = it }
      pageToken?.let { this.pageToken = it }
    },
  )
