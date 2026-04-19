package xyz.mcxross.ksui.grpc.internal

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetBalanceRequest
import sui.rpc.v2.GetBalanceRequestInternal
import sui.rpc.v2.GetBalanceResponse
import sui.rpc.v2.GetCoinInfoRequest
import sui.rpc.v2.GetCoinInfoRequestInternal
import sui.rpc.v2.GetCoinInfoResponse
import sui.rpc.v2.ListBalancesRequest
import sui.rpc.v2.ListBalancesRequestInternal
import sui.rpc.v2.ListBalancesResponse
import xyz.mcxross.ksui.model.AccountAddress

internal suspend fun getCoinInfo(
  runtime: GrpcRuntime,
  request: GetCoinInfoRequest,
): GetCoinInfoResponse = runtime.stateService.GetCoinInfo(request)

internal suspend fun getCoinInfo(runtime: GrpcRuntime, coinType: String): GetCoinInfoResponse =
  getCoinInfo(runtime, GetCoinInfoRequestInternal().apply { this.coinType = coinType })

internal suspend fun getBalance(
  runtime: GrpcRuntime,
  request: GetBalanceRequest,
): GetBalanceResponse = runtime.stateService.GetBalance(request)

internal suspend fun getBalance(
  runtime: GrpcRuntime,
  owner: String,
  coinType: String? = null,
): GetBalanceResponse =
  getBalance(
    runtime,
    GetBalanceRequestInternal().apply {
      this.owner = owner
      this.coinType = coinType ?: DEFAULT_SUI_COIN_TYPE
    },
  )

internal suspend fun getBalance(
  runtime: GrpcRuntime,
  owner: AccountAddress,
  coinType: String? = null,
): GetBalanceResponse = getBalance(runtime, owner.toString(), coinType)

internal suspend fun listBalances(
  runtime: GrpcRuntime,
  request: ListBalancesRequest,
): ListBalancesResponse = runtime.stateService.ListBalances(request)

internal suspend fun listBalances(
  runtime: GrpcRuntime,
  owner: String,
  pageSize: UInt? = null,
  pageToken: ByteString? = null,
): ListBalancesResponse =
  listBalances(
    runtime,
    ListBalancesRequestInternal().apply {
      this.owner = owner
      pageSize?.let { this.pageSize = it }
      pageToken?.let { this.pageToken = it }
    },
  )

internal suspend fun listBalances(
  runtime: GrpcRuntime,
  owner: AccountAddress,
  pageSize: UInt? = null,
  pageToken: ByteString? = null,
): ListBalancesResponse = listBalances(runtime, owner.toString(), pageSize, pageToken)
