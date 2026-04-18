package xyz.mcxross.ksui.grpc.protocol

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetBalanceRequest
import sui.rpc.v2.GetBalanceResponse
import sui.rpc.v2.GetCoinInfoRequest
import sui.rpc.v2.GetCoinInfoResponse
import sui.rpc.v2.ListBalancesRequest
import sui.rpc.v2.ListBalancesResponse
import xyz.mcxross.ksui.model.AccountAddress

interface Coin {
  suspend fun getCoinInfo(request: GetCoinInfoRequest): GetCoinInfoResponse

  suspend fun getCoinInfo(coinType: String): GetCoinInfoResponse

  suspend fun getBalance(request: GetBalanceRequest): GetBalanceResponse

  suspend fun getBalance(owner: String, coinType: String? = null): GetBalanceResponse

  suspend fun getBalance(owner: AccountAddress, coinType: String? = null): GetBalanceResponse

  suspend fun listBalances(request: ListBalancesRequest): ListBalancesResponse

  suspend fun listBalances(
    owner: String,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
  ): ListBalancesResponse

  suspend fun listBalances(
    owner: AccountAddress,
    pageSize: UInt? = null,
    pageToken: ByteString? = null,
  ): ListBalancesResponse
}
