package xyz.mcxross.ksui.grpc.protocol

import sui.rpc.v2.LookupNameRequest
import sui.rpc.v2.LookupNameResponse
import sui.rpc.v2.ReverseLookupNameRequest
import sui.rpc.v2.ReverseLookupNameResponse
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result

interface Sns {
  @Throws(Exception::class)
  suspend fun lookupName(request: LookupNameRequest): Result<LookupNameResponse, SuiError>

  @Throws(Exception::class)
  suspend fun lookupName(name: String): Result<LookupNameResponse, SuiError>

  @Throws(Exception::class)
  suspend fun reverseLookupName(
    request: ReverseLookupNameRequest
  ): Result<ReverseLookupNameResponse, SuiError>

  @Throws(Exception::class)
  suspend fun reverseLookupName(address: String): Result<ReverseLookupNameResponse, SuiError>

  @Throws(Exception::class)
  suspend fun reverseLookupName(
    address: AccountAddress
  ): Result<ReverseLookupNameResponse, SuiError>
}
