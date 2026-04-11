package xyz.mcxross.ksui.grpc.protocol

import sui.rpc.v2.LookupNameRequest
import sui.rpc.v2.LookupNameResponse
import sui.rpc.v2.ReverseLookupNameRequest
import sui.rpc.v2.ReverseLookupNameResponse
import xyz.mcxross.ksui.model.AccountAddress

interface Sns {
  suspend fun lookupName(request: LookupNameRequest): LookupNameResponse

  suspend fun lookupName(name: String): LookupNameResponse

  suspend fun reverseLookupName(request: ReverseLookupNameRequest): ReverseLookupNameResponse

  suspend fun reverseLookupName(address: String): ReverseLookupNameResponse

  suspend fun reverseLookupName(address: AccountAddress): ReverseLookupNameResponse
}
