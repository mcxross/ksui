package xyz.mcxross.ksui.grpc.internal

import sui.rpc.v2.LookupNameRequest
import sui.rpc.v2.LookupNameRequestInternal
import sui.rpc.v2.LookupNameResponse
import sui.rpc.v2.ReverseLookupNameRequest
import sui.rpc.v2.ReverseLookupNameRequestInternal
import sui.rpc.v2.ReverseLookupNameResponse
import xyz.mcxross.ksui.model.AccountAddress

internal suspend fun lookupName(
  runtime: GrpcRuntime,
  request: LookupNameRequest,
): LookupNameResponse = runtime.nameService.LookupName(request)

internal suspend fun lookupName(runtime: GrpcRuntime, name: String): LookupNameResponse =
  lookupName(runtime, LookupNameRequestInternal().apply { this.name = name })

internal suspend fun reverseLookupName(
  runtime: GrpcRuntime,
  request: ReverseLookupNameRequest,
): ReverseLookupNameResponse = runtime.nameService.ReverseLookupName(request)

internal suspend fun reverseLookupName(
  runtime: GrpcRuntime,
  address: String,
): ReverseLookupNameResponse =
  reverseLookupName(runtime, ReverseLookupNameRequestInternal().apply { this.address = address })

internal suspend fun reverseLookupName(
  runtime: GrpcRuntime,
  address: AccountAddress,
): ReverseLookupNameResponse = reverseLookupName(runtime, address.toString())
