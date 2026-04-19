package xyz.mcxross.ksui.grpc.api

import sui.rpc.v2.LookupNameRequest
import sui.rpc.v2.LookupNameResponse
import sui.rpc.v2.ReverseLookupNameRequest
import sui.rpc.v2.ReverseLookupNameResponse
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.handleGrpc
import xyz.mcxross.ksui.grpc.internal.lookupName as internalLookupName
import xyz.mcxross.ksui.grpc.internal.reverseLookupName as internalReverseLookupName
import xyz.mcxross.ksui.grpc.protocol.Sns as SnsProtocol
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

internal class Sns(private val runtime: GrpcRuntime) : SnsProtocol {
  override suspend fun lookupName(
    request: LookupNameRequest
  ): Result<LookupNameResponse, SuiError> = handleGrpc { internalLookupName(runtime, request) }

  override suspend fun lookupName(name: String): Result<LookupNameResponse, SuiError> = handleGrpc {
    internalLookupName(runtime, name)
  }

  override suspend fun reverseLookupName(
    request: ReverseLookupNameRequest
  ): Result<ReverseLookupNameResponse, SuiError> = handleGrpc {
    internalReverseLookupName(runtime, request)
  }

  override suspend fun reverseLookupName(
    address: String
  ): Result<ReverseLookupNameResponse, SuiError> = handleGrpc {
    internalReverseLookupName(runtime, address)
  }

  override suspend fun reverseLookupName(
    address: AccountAddress
  ): Result<ReverseLookupNameResponse, SuiError> = handleGrpc {
    internalReverseLookupName(runtime, address)
  }
}
