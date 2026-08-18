/*
 * Copyright 2025 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.core.crypto

import java.math.BigInteger
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.model.PasskeyAuthenticator

@Serializable
private data class ClientDataJson(val type: String, val challenge: String, val origin: String)

private const val AUTHENTICATOR_DATA_MIN_SIZE = 37
private const val USER_PRESENT_FLAG = 0x01
private const val USER_VERIFIED_FLAG = 0x04
private val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
private val clientDataFormat = Json { ignoreUnknownKeys = true }

private fun sha256(data: ByteArray): ByteArray =
  SHA256Digest().let {
    it.update(data, 0, data.size)
    ByteArray(it.digestSize).also { out -> it.doFinal(out, 0) }
  }

/**
 * Parses the full signature byte array from the Sui network. It strips the 0x06 flag and
 * BCS-decodes the payload.
 */
private fun parseSerializedPasskeySignature(signature: ByteArray): PasskeyAuthenticator {
  if (signature.isEmpty() || signature.first() != SignatureScheme.PASSKEY.scheme) {
    throw IllegalArgumentException("Invalid Passkey signature scheme")
  }
  // Remove the 0x06 flag before decoding
  val bcsPayload = signature.copyOfRange(1, signature.size)
  return Bcs.decodeFromByteArray<PasskeyAuthenticator>(bcsPayload)
}

internal fun passkeyAssertionContextIsValid(
  clientDataJson: String,
  authenticatorData: ByteArray,
  message: ByteArray,
  expectedRpId: String,
  expectedOrigin: String,
  requireUserVerification: Boolean,
): Boolean {
  if (authenticatorData.size < AUTHENTICATOR_DATA_MIN_SIZE) return false

  val clientData =
    try {
      clientDataFormat.decodeFromString<ClientDataJson>(clientDataJson)
    } catch (_: Exception) {
      return false
    }
  if (clientData.type != "webauthn.get" || clientData.origin != expectedOrigin) return false

  val decodedChallenge =
    try {
      base64Url.decode(clientData.challenge)
    } catch (_: Exception) {
      return false
    }
  if (!message.contentEquals(decodedChallenge)) return false

  val expectedRpIdHash = sha256(expectedRpId.toByteArray(Charsets.UTF_8))
  if (!expectedRpIdHash.contentEquals(authenticatorData.copyOfRange(0, 32))) return false

  val flags = authenticatorData[32].toInt() and 0xff
  if (flags and USER_PRESENT_FLAG == 0) return false
  if (requireUserVerification && flags and USER_VERIFIED_FLAG == 0) return false

  return true
}

/**
 * Verifies only the Sui proof-of-key signature. It intentionally has no relying-party context and
 * must not be used as a complete WebAuthn authentication decision.
 */
internal fun verifyPasskeySignature(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
): Boolean = verifyPasskeySignatureInternal(publicKey, message, signature)

/** Verifies a Passkey assertion and its relying-party ceremony policy. */
internal fun verifyPasskeyAssertion(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
  expectedRpId: String,
  expectedOrigin: String,
  requireUserVerification: Boolean = true,
): Boolean =
  verifyPasskeySignatureInternal(
    publicKey = publicKey,
    message = message,
    signature = signature,
    expectedRpId = expectedRpId,
    expectedOrigin = expectedOrigin,
    requireUserVerification = requireUserVerification,
  )

private fun verifyPasskeySignatureInternal(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
  expectedRpId: String? = null,
  expectedOrigin: String? = null,
  requireUserVerification: Boolean = false,
): Boolean {
  try {
    val parsed = parseSerializedPasskeySignature(signature)

    val clientData = clientDataFormat.decodeFromString<ClientDataJson>(parsed.clientDataJson)

    if (clientData.type != "webauthn.get") return false
    val decodedChallenge = base64Url.decode(clientData.challenge)
    if (!message.contentEquals(decodedChallenge)) return false

    if (expectedRpId != null || expectedOrigin != null) {
      if (expectedRpId == null || expectedOrigin == null) return false
      if (
        !passkeyAssertionContextIsValid(
          clientDataJson = parsed.clientDataJson,
          authenticatorData = parsed.authenticatorData,
          message = message,
          expectedRpId = expectedRpId,
          expectedOrigin = expectedOrigin,
          requireUserVerification = requireUserVerification,
        )
      ) {
        return false
      }
    }

    if (publicKey.data.size != 33) return false
    if (parsed.userSignature.size != 1 + 64 + publicKey.data.size) return false
    if (parsed.userSignature[0] != SignatureScheme.Secp256r1.scheme) return false
    val pkFromSignature = parsed.userSignature.copyOfRange(1 + 64, 1 + 64 + publicKey.data.size)
    if (!publicKey.data.contentEquals(pkFromSignature)) return false

    val clientDataJsonBytes = parsed.clientDataJson.toByteArray(Charsets.UTF_8)
    val clientDataJsonDigest = sha256(clientDataJsonBytes)
    val signedMessageBytes = parsed.authenticatorData + clientDataJsonDigest
    val messageHash = sha256(signedMessageBytes)

    val r = BigInteger(1, parsed.userSignature.copyOfRange(1, 1 + 32))
    val s = BigInteger(1, parsed.userSignature.copyOfRange(1 + 32, 1 + 64))

    val curveParams = org.bouncycastle.crypto.ec.CustomNamedCurves.getByName("secp256r1")
    val point = curveParams.curve.decodePoint(publicKey.data)
    val keyParameters =
      ECPublicKeyParameters(
        point,
        org.bouncycastle.crypto.params.ECDomainParameters(
          curveParams.curve,
          curveParams.g,
          curveParams.n,
          curveParams.h,
        ),
      )

    val signer = ECDSASigner()
    signer.init(false, keyParameters)
    return signer.verifySignature(messageHash, r, s)
  } catch (e: Exception) {
    return false
  }
}
