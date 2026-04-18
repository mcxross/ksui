package xyz.mcxross.ksui.grpc.protocol

import sui.rpc.v2.ActiveJwk
import sui.rpc.v2.Bcs
import sui.rpc.v2.UserSignature
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponse

interface Signature {
  suspend fun verifySignature(request: VerifySignatureRequest): VerifySignatureResponse

  suspend fun verifySignature(
    message: Bcs,
    signature: UserSignature,
    address: String? = null,
    jwks: List<ActiveJwk> = emptyList(),
  ): VerifySignatureResponse

  suspend fun verifySignature(
    messageBytes: ByteArray,
    signatureBytes: ByteArray,
    messageName: String = "TransactionData",
    signatureName: String = "UserSignature",
    address: String? = null,
    jwks: List<ActiveJwk> = emptyList(),
  ): VerifySignatureResponse
}
