package xyz.mcxross.ksui.grpc.api

import sui.rpc.v2.ActiveJwk
import sui.rpc.v2.Bcs
import sui.rpc.v2.UserSignature
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponse
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.handleGrpc
import xyz.mcxross.ksui.grpc.internal.verifySignature as internalVerifySignature
import xyz.mcxross.ksui.grpc.protocol.Signature as SignatureProtocol
import xyz.mcxross.ksui.model.Result

internal class Signature(private val runtime: GrpcRuntime) : SignatureProtocol {
  override suspend fun verifySignature(
    request: VerifySignatureRequest
  ): Result<VerifySignatureResponse, SuiError> = handleGrpc {
    internalVerifySignature(runtime, request)
  }

  override suspend fun verifySignature(
    message: Bcs,
    signature: UserSignature,
    address: String?,
    jwks: List<ActiveJwk>,
  ): Result<VerifySignatureResponse, SuiError> = handleGrpc {
    internalVerifySignature(runtime, message, signature, address, jwks)
  }

  override suspend fun verifySignature(
    messageBytes: ByteArray,
    signatureBytes: ByteArray,
    messageName: String,
    signatureName: String,
    address: String?,
    jwks: List<ActiveJwk>,
  ): Result<VerifySignatureResponse, SuiError> = handleGrpc {
    internalVerifySignature(
      runtime,
      messageBytes,
      signatureBytes,
      messageName,
      signatureName,
      address,
      jwks,
    )
  }
}
