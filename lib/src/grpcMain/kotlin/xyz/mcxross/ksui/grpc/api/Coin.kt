package xyz.mcxross.ksui.grpc.api

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetBalanceRequest
import sui.rpc.v2.GetBalanceResponse
import sui.rpc.v2.GetCoinInfoRequest
import sui.rpc.v2.GetCoinInfoResponse
import sui.rpc.v2.ListBalancesRequest
import sui.rpc.v2.ListBalancesResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.getBalance as internalGetBalance
import xyz.mcxross.ksui.grpc.internal.getCoinInfo as internalGetCoinInfo
import xyz.mcxross.ksui.grpc.internal.listBalances as internalListBalances
import xyz.mcxross.ksui.grpc.protocol.Coin as CoinProtocol
import xyz.mcxross.ksui.model.AccountAddress

internal class Coin(private val runtime: GrpcRuntime) : CoinProtocol {
  override suspend fun getCoinInfo(request: GetCoinInfoRequest): GetCoinInfoResponse =
    internalGetCoinInfo(runtime, request)

  override suspend fun getCoinInfo(coinType: String): GetCoinInfoResponse =
    internalGetCoinInfo(runtime, coinType)

  override suspend fun getBalance(request: GetBalanceRequest): GetBalanceResponse =
    internalGetBalance(runtime, request)

  override suspend fun getBalance(owner: String, coinType: String?): GetBalanceResponse =
    internalGetBalance(runtime, owner, coinType)

  override suspend fun getBalance(owner: AccountAddress, coinType: String?): GetBalanceResponse =
    internalGetBalance(runtime, owner, coinType)

  override suspend fun listBalances(request: ListBalancesRequest): ListBalancesResponse =
    internalListBalances(runtime, request)

  override suspend fun listBalances(
    owner: String,
    pageSize: UInt?,
    pageToken: ByteString?,
  ): ListBalancesResponse = internalListBalances(runtime, owner, pageSize, pageToken)

  override suspend fun listBalances(
    owner: AccountAddress,
    pageSize: UInt?,
    pageToken: ByteString?,
  ): ListBalancesResponse = internalListBalances(runtime, owner, pageSize, pageToken)
}
