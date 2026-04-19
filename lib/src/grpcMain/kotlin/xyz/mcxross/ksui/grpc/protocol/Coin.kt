package xyz.mcxross.ksui.grpc.protocol

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetBalanceRequest
import sui.rpc.v2.GetBalanceResponse
import sui.rpc.v2.GetCoinInfoRequest
import sui.rpc.v2.GetCoinInfoResponse
import sui.rpc.v2.ListBalancesRequest
import sui.rpc.v2.ListBalancesResponse
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

interface Coin {
  suspend fun getCoinInfo(request: GetCoinInfoRequest): Result<GetCoinInfoResponse, SuiError>

  suspend fun getCoinInfo(coinType: String): Result<GetCoinInfoResponse, SuiError>

  suspend fun getBalance(request: GetBalanceRequest): Result<GetBalanceResponse, SuiError>

  suspend fun getBalance(
    owner: String,
    coinType: String? = null,
  ): Result<GetBalanceResponse, SuiError>

  suspend fun getBalance(
    owner: AccountAddress,
    coinType: String? = null,
  ): Result<GetBalanceResponse, SuiError>

  suspend fun listBalances(request: ListBalancesRequest): Result<ListBalancesResponse, SuiError>

  suspend fun listBalances(
    owner: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
  ): Result<ListBalancesResponse, SuiError>

  suspend fun listBalances(
    owner: AccountAddress,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
  ): Result<ListBalancesResponse, SuiError>
}
