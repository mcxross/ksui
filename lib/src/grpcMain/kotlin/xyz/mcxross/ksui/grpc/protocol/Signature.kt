package xyz.mcxross.ksui.grpc.protocol

import sui.rpc.v2.ActiveJwk
import sui.rpc.v2.Bcs
import sui.rpc.v2.UserSignature
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponse
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.model.Result

interface Signature {
  suspend fun verifySignature(
    request: VerifySignatureRequest
  ): Result<VerifySignatureResponse, SuiError>

  suspend fun verifySignature(
    message: Bcs,
    signature: UserSignature,
    address: String? = null,
    jwks: List<ActiveJwk> = emptyList(),
  ): Result<VerifySignatureResponse, SuiError>

  suspend fun verifySignature(
    messageBytes: ByteArray,
    signatureBytes: ByteArray,
    messageName: String = "TransactionData",
    signatureName: String = "UserSignature",
    address: String? = null,
    jwks: List<ActiveJwk> = emptyList(),
  ): Result<VerifySignatureResponse, SuiError>
}
