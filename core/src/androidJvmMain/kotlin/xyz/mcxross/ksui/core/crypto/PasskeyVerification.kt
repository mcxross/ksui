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
import xyz.mcxross.ksui.model.PasskeyAuthenticator

@Serializable
private data class ClientDataJson(val type: String, val challenge: String, val origin: String)

/**
 * Parses the full signature byte array from the Sui network. It strips the 0x06 flag and
 * BCS-decodes the payload.
 */
private fun parseSerializedPasskeySignature(signature: ByteArray): PasskeyAuthenticator {
  if (signature.first() != 0x06.toByte()) {
    throw IllegalArgumentException("Invalid Passkey signature scheme")
  }
  // Remove the 0x06 flag before decoding
  val bcsPayload = signature.copyOfRange(1, signature.size)
  return Bcs.decodeFromByteArray<PasskeyAuthenticator>(bcsPayload)
}

/** Verifies a Passkey signature against an original message (challenge). */
internal fun verifyPasskeySignature(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
): Boolean {
  try {
    val parsed = parseSerializedPasskeySignature(signature)

    val json = Json { ignoreUnknownKeys = true }
    val clientData = json.decodeFromString<ClientDataJson>(parsed.clientDataJson)

    if (clientData.type != "webauthn.get") return false
    val decodedChallenge = Base64.UrlSafe.decode(clientData.challenge)
    if (!message.contentEquals(decodedChallenge)) return false

    val pkFromSignature = parsed.userSignature.copyOfRange(1 + 64, parsed.userSignature.size)
    if (!publicKey.data.contentEquals(pkFromSignature)) return false

    val clientDataJsonBytes = parsed.clientDataJson.toByteArray(Charsets.UTF_8)
    val clientDataJsonDigest =
      SHA256Digest().let {
        it.update(clientDataJsonBytes, 0, clientDataJsonBytes.size)
        ByteArray(it.digestSize).also { out -> it.doFinal(out, 0) }
      }
    val signedMessageBytes = parsed.authenticatorData + clientDataJsonDigest
    val messageHash =
      SHA256Digest().let {
        it.update(signedMessageBytes, 0, signedMessageBytes.size)
        ByteArray(it.digestSize).also { out -> it.doFinal(out, 0) }
      }

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
