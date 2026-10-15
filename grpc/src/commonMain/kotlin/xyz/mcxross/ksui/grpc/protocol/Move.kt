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
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result

interface Move {
  @Throws(Exception::class)
  suspend fun getPackage(request: GetPackageRequest): Result<GetPackageResponse, SuiError>

  @Throws(Exception::class)
  suspend fun getPackage(packageId: String): Result<GetPackageResponse, SuiError>

  @Throws(Exception::class)
  suspend fun getDatatype(request: GetDatatypeRequest): Result<GetDatatypeResponse, SuiError>

  @Throws(Exception::class)
  suspend fun getDatatype(
    packageId: String,
    moduleName: String,
    name: String,
  ): Result<GetDatatypeResponse, SuiError>

  @Throws(Exception::class)
  suspend fun getFunction(request: GetFunctionRequest): Result<GetFunctionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun getFunction(
    packageId: String,
    moduleName: String,
    name: String,
  ): Result<GetFunctionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun listPackageVersions(
    request: ListPackageVersionsRequest
  ): Result<ListPackageVersionsResponse, SuiError>

  @Throws(Exception::class)
  suspend fun listPackageVersions(
    packageId: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
  ): Result<ListPackageVersionsResponse, SuiError>
}
