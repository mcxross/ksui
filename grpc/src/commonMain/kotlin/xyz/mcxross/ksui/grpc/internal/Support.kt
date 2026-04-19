package xyz.mcxross.ksui.grpc.internal

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.BcsInternal
import sui.rpc.v2.Transaction as GrpcTransaction
import sui.rpc.v2.TransactionInternal
import sui.rpc.v2.UserSignature
import sui.rpc.v2.UserSignatureInternal

internal const val DEFAULT_SUI_COIN_TYPE =
  "0x0000000000000000000000000000000000000000000000000000000000000002::sui::SUI"

internal fun ByteArray.asGrpcTransaction(): GrpcTransaction =
  TransactionInternal().apply {
    bcs = BcsInternal().apply { value = ByteString(this@asGrpcTransaction) }
  }

internal fun ByteArray.asGrpcSignature(): UserSignature =
  UserSignatureInternal().apply {
    bcs = BcsInternal().apply { value = ByteString(this@asGrpcSignature) }
  }
