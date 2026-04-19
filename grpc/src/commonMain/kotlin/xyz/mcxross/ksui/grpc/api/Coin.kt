package xyz.mcxross.ksui.grpc.api

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.GetBalanceRequest
import sui.rpc.v2.GetBalanceResponse
import sui.rpc.v2.GetCoinInfoRequest
import sui.rpc.v2.GetCoinInfoResponse
import sui.rpc.v2.ListBalancesRequest
import sui.rpc.v2.ListBalancesResponse
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.getBalance as internalGetBalance
import xyz.mcxross.ksui.grpc.internal.getCoinInfo as internalGetCoinInfo
import xyz.mcxross.ksui.grpc.internal.handleGrpc
import xyz.mcxross.ksui.grpc.internal.listBalances as internalListBalances
import xyz.mcxross.ksui.grpc.protocol.Coin as CoinProtocol

internal class Coin(private val runtime: GrpcRuntime) : CoinProtocol {
  override suspend fun getCoinInfo(
    request: GetCoinInfoRequest
  ): Result<GetCoinInfoResponse, SuiError> = handleGrpc { internalGetCoinInfo(runtime, request) }

  override suspend fun getCoinInfo(coinType: String): Result<GetCoinInfoResponse, SuiError> =
    handleGrpc {
      internalGetCoinInfo(runtime, coinType)
    }

  override suspend fun getBalance(
    request: GetBalanceRequest
  ): Result<GetBalanceResponse, SuiError> = handleGrpc { internalGetBalance(runtime, request) }

  override suspend fun getBalance(
    owner: String,
    coinType: String?,
  ): Result<GetBalanceResponse, SuiError> = handleGrpc {
    internalGetBalance(runtime, owner, coinType)
  }

  override suspend fun getBalance(
    owner: AccountAddress,
    coinType: String?,
  ): Result<GetBalanceResponse, SuiError> = handleGrpc {
    internalGetBalance(runtime, owner, coinType)
  }

  override suspend fun listBalances(
    request: ListBalancesRequest
  ): Result<ListBalancesResponse, SuiError> = handleGrpc { internalListBalances(runtime, request) }

  override suspend fun listBalances(
    owner: String,
    pageSize: UInt?,
    pageToken: ByteString?,
  ): Result<ListBalancesResponse, SuiError> = handleGrpc {
    internalListBalances(runtime, owner, pageSize, pageToken)
  }

  override suspend fun listBalances(
    owner: AccountAddress,
    pageSize: UInt?,
    pageToken: ByteString?,
  ): Result<ListBalancesResponse, SuiError> = handleGrpc {
    internalListBalances(runtime, owner, pageSize, pageToken)
  }
}
