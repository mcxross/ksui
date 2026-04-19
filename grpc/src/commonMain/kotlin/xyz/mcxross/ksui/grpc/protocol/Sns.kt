package xyz.mcxross.ksui.grpc.protocol

import sui.rpc.v2.LookupNameRequest
import sui.rpc.v2.LookupNameResponse
import sui.rpc.v2.ReverseLookupNameRequest
import sui.rpc.v2.ReverseLookupNameResponse
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

interface Sns {
  suspend fun lookupName(request: LookupNameRequest): Result<LookupNameResponse, SuiError>

  suspend fun lookupName(name: String): Result<LookupNameResponse, SuiError>

  suspend fun reverseLookupName(
    request: ReverseLookupNameRequest
  ): Result<ReverseLookupNameResponse, SuiError>

  suspend fun reverseLookupName(address: String): Result<ReverseLookupNameResponse, SuiError>

  suspend fun reverseLookupName(
    address: AccountAddress
  ): Result<ReverseLookupNameResponse, SuiError>
}
