package xyz.mcxross.ksui.grpc.api

import sui.rpc.v2.LookupNameRequest
import sui.rpc.v2.LookupNameResponse
import sui.rpc.v2.ReverseLookupNameRequest
import sui.rpc.v2.ReverseLookupNameResponse
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.lookupName as internalLookupName
import xyz.mcxross.ksui.grpc.internal.reverseLookupName as internalReverseLookupName
import xyz.mcxross.ksui.grpc.protocol.Sns as SnsProtocol
import xyz.mcxross.ksui.model.AccountAddress

internal class Sns(private val runtime: GrpcRuntime) : SnsProtocol {
  override suspend fun lookupName(request: LookupNameRequest): LookupNameResponse =
    internalLookupName(runtime, request)

  override suspend fun lookupName(name: String): LookupNameResponse =
    internalLookupName(runtime, name)

  override suspend fun reverseLookupName(
    request: ReverseLookupNameRequest
  ): ReverseLookupNameResponse = internalReverseLookupName(runtime, request)

  override suspend fun reverseLookupName(address: String): ReverseLookupNameResponse =
    internalReverseLookupName(runtime, address)

  override suspend fun reverseLookupName(address: AccountAddress): ReverseLookupNameResponse =
    internalReverseLookupName(runtime, address)
}
