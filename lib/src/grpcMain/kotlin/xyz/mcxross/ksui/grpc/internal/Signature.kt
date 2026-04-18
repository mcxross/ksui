package xyz.mcxross.ksui.grpc.internal

import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.ActiveJwk
import sui.rpc.v2.Bcs
import sui.rpc.v2.BcsInternal
import sui.rpc.v2.UserSignature
import sui.rpc.v2.UserSignatureInternal
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureRequestInternal
import sui.rpc.v2.VerifySignatureResponse

internal suspend fun verifySignature(
  runtime: GrpcRuntime,
  request: VerifySignatureRequest,
): VerifySignatureResponse = runtime.signatureVerificationService.VerifySignature(request)

internal suspend fun verifySignature(
  runtime: GrpcRuntime,
  message: Bcs,
  signature: UserSignature,
  address: String? = null,
  jwks: List<ActiveJwk> = emptyList(),
): VerifySignatureResponse =
  verifySignature(
    runtime,
    VerifySignatureRequestInternal().apply {
      this.message = message
      this.signature = signature
      address?.let { this.address = it }
      this.jwks = jwks
    },
  )

internal suspend fun verifySignature(
  runtime: GrpcRuntime,
  messageBytes: ByteArray,
  signatureBytes: ByteArray,
  messageName: String = "TransactionData",
  signatureName: String = "UserSignature",
  address: String? = null,
  jwks: List<ActiveJwk> = emptyList(),
): VerifySignatureResponse =
  verifySignature(
    runtime,
    message =
      BcsInternal().apply {
        name = messageName
        value = ByteString(messageBytes)
      },
    signature =
      UserSignatureInternal().apply {
        bcs =
          BcsInternal().apply {
            name = signatureName
            value = ByteString(signatureBytes)
          }
      },
    address = address,
    jwks = jwks,
  )
