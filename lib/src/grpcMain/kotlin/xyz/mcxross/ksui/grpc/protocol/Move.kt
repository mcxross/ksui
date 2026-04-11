package xyz.mcxross.ksui.grpc.protocol

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetDatatypeRequest
import sui.rpc.v2.GetDatatypeResponse
import sui.rpc.v2.GetFunctionRequest
import sui.rpc.v2.GetFunctionResponse
import sui.rpc.v2.GetPackageRequest
import sui.rpc.v2.GetPackageResponse
import sui.rpc.v2.ListPackageVersionsRequest
import sui.rpc.v2.ListPackageVersionsResponse

interface Move {
  suspend fun getPackage(request: GetPackageRequest): GetPackageResponse

  suspend fun getPackage(packageId: String): GetPackageResponse

  suspend fun getDatatype(request: GetDatatypeRequest): GetDatatypeResponse

  suspend fun getDatatype(packageId: String, moduleName: String, name: String): GetDatatypeResponse

  suspend fun getFunction(request: GetFunctionRequest): GetFunctionResponse

  suspend fun getFunction(packageId: String, moduleName: String, name: String): GetFunctionResponse

  suspend fun listPackageVersions(request: ListPackageVersionsRequest): ListPackageVersionsResponse

  suspend fun listPackageVersions(
    packageId: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
  ): ListPackageVersionsResponse
}
