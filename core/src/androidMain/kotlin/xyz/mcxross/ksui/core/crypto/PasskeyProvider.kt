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

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.credentials.*
import androidx.credentials.exceptions.*
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.account.PasskeyAccount
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.AuthenticationResponse
import xyz.mcxross.ksui.core.model.PasskeyAuthenticator
import xyz.mcxross.ksui.core.model.RegistrationResponses
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.utils.PasskeyUtils.recoverPublicKeyPoint

@Serializable
private data class WebAuthnRp(val id: String, val name: String)

@Serializable
private data class WebAuthnUser(val id: String, val name: String, val displayName: String)

@Serializable
private data class WebAuthnCredParam(val type: String, val alg: Int)

@Serializable
private data class WebAuthnAuthenticatorSelection(
  val authenticatorAttachment: String,
  val requireResidentKey: Boolean,
  val userVerification: String,
)

@Serializable
private data class WebAuthnCreateRequest(
  val rp: WebAuthnRp,
  val user: WebAuthnUser,
  val challenge: String,
  val pubKeyCredParams: List<WebAuthnCredParam>,
  val authenticatorSelection: WebAuthnAuthenticatorSelection,
)

@Serializable
private data class WebAuthnGetRequest(
  val rpId: String,
  val challenge: String,
  val userVerification: String,
)

actual class PasskeyProvider(
  private val context: Context,
  private val rpId: String,
  private val expectedOrigin: String = applicationOrigin(context),
) {

  private val credentialManager = CredentialManager.create(context)

  private val base64UrlEncoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

  private val json = Json { ignoreUnknownKeys = true }

  @SuppressLint("PublicKeyCredential")
  internal actual suspend fun create(
    name: String,
    displayName: String,
    userId: String,
    challenge: String,
  ): Result<PasskeyAccount, E> {
    val requestJson =
      json.encodeToString(
        WebAuthnCreateRequest(
          rp = WebAuthnRp(id = rpId, name = "Sui Passkey dApp"),
          user =
            WebAuthnUser(
              id = base64UrlEncoder.encode(userId.encodeToByteArray()),
              name = name,
              displayName = displayName,
            ),
          challenge = base64UrlEncoder.encode(challenge.encodeToByteArray()),
          pubKeyCredParams = listOf(WebAuthnCredParam(type = "public-key", alg = -7)),
          authenticatorSelection =
            WebAuthnAuthenticatorSelection(
              authenticatorAttachment = "platform",
              requireResidentKey = true,
              userVerification = "required",
            ),
        )
      )

    val request = CreatePublicKeyCredentialRequest(requestJson)

    try {
      val result = credentialManager.createCredential(context, request)

      val registrationResponseJson =
        result.data.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")

      if (registrationResponseJson != null) {
        val registrationResponse =
          json.decodeFromString<RegistrationResponses>(registrationResponseJson)

        val publicKeySpkiBytes =
          android.util.Base64.decode(
            registrationResponse.response.publicKey,
            android.util.Base64.URL_SAFE,
          )

        val spki = SubjectPublicKeyInfo.getInstance(publicKeySpkiBytes)
        val point =
          CustomNamedCurves.getByName("secp256r1").curve.decodePoint(spki.publicKeyData.bytes)
        val publicKeyBytes = point.getEncoded(true)

        return Result.Ok(PasskeyAccount(PasskeyPublicKey(publicKeyBytes), this))
      } else {
        return Result.Err(
          IllegalStateException(
            "Passkey result Bundle error: Couldn't retrieve registrationResponseJson"
          )
        )
      }
    } catch (e: CreateCredentialException) {
      when (e) {
        is CreatePublicKeyCredentialDomException -> {
          return Result.Err(IllegalStateException("Passkey DOM error: ${e.domError}", e))
        }
        is CreateCredentialCancellationException -> {
          return Result.Err(IllegalStateException("User canceled Passkey creation.", e))
        }
        is CreateCredentialInterruptedException -> {
          return Result.Err(
            IllegalStateException("Passkey creation was interrupted. Please try again.", e)
          )
        }
        is CreateCredentialProviderConfigurationException -> {
          return Result.Err(IllegalStateException("Device not configured for Passkeys.", e))
        }
        else -> {
          return Result.Err(IllegalStateException("Failed to create Passkey: ${e.message}", e))
        }
      }
    }
  }

  internal actual suspend fun sign(pk: ByteArray, challenge: ByteArray): Result<ByteArray, E> {
    val requestJson =
      json.encodeToString(
        WebAuthnGetRequest(
          rpId = rpId,
          challenge = base64UrlEncoder.encode(challenge),
          userVerification = "required",
        )
      )
    val request =
      GetCredentialRequest(credentialOptions = listOf(GetPublicKeyCredentialOption(requestJson)))

    try {
      val result = credentialManager.getCredential(context, request)
      val publicKeyCredential = result.credential as PublicKeyCredential
      val credentials =
        json.decodeFromString<AuthenticationResponse>(
          publicKeyCredential.authenticationResponseJson
        )

      val authenticatorData = base64UrlEncoder.decode(credentials.response.authenticatorData)
      val signatureDER = base64UrlEncoder.decode(credentials.response.signature)
      val clientDataJsonString =
        base64UrlEncoder.decode(credentials.response.clientDataJSON).toString(Charsets.UTF_8)

      val normalizedSig = normalizeSignature(signatureDER)
      val secp256r1Flag: Byte = 0x02
      val userSignature =
        ByteArray(1 + normalizedSig.size + pk.size).apply {
          this[0] = secp256r1Flag
          System.arraycopy(normalizedSig, 0, this, 1, normalizedSig.size)
          System.arraycopy(pk, 0, this, 1 + normalizedSig.size, pk.size)
        }

      val bcsPayload =
        Bcs.encodeToByteArray(
          PasskeyAuthenticator(authenticatorData, clientDataJsonString, userSignature)
        )

      val passkeyFlag: Byte = 0x06
      return Result.Ok(
        ByteArray(1 + bcsPayload.size).apply {
          this[0] = passkeyFlag
          System.arraycopy(bcsPayload, 0, this, 1, bcsPayload.size)
        }
      )
    } catch (e: GetCredentialException) {
      return Result.Err(IllegalStateException("Signing failed: ${e.message}", e))
    }
  }

  internal actual fun verify(
    pk: ByteArray,
    challenge: ByteArray,
    signature: ByteArray,
  ): Result<Boolean, E> =
    Result.Ok(
      verifyPasskeyAssertion(
        publicKey = PasskeyPublicKey(pk),
        message = challenge,
        signature = signature,
        expectedRpId = rpId,
        expectedOrigin = expectedOrigin,
        requireUserVerification = true,
      )
    )

  suspend fun signAndRecover(message: ByteArray): List<PasskeyPublicKey> {
    val requestJson =
      json.encodeToString(
        WebAuthnGetRequest(
          rpId = rpId,
          challenge = base64UrlEncoder.encode(message),
          userVerification = "required",
        )
      )
    val request =
      GetCredentialRequest(credentialOptions = listOf(GetPublicKeyCredentialOption(requestJson)))

    try {
      val result = credentialManager.getCredential(context, request)
      val publicKeyCredential = result.credential as PublicKeyCredential
      val credentials =
        json.decodeFromString<AuthenticationResponse>(
          publicKeyCredential.authenticationResponseJson
        )

      val authenticatorData = base64UrlEncoder.decode(credentials.response.authenticatorData)
      val signatureDER = base64UrlEncoder.decode(credentials.response.signature)
      val clientDataJsonBytes = base64UrlEncoder.decode(credentials.response.clientDataJSON)

      val clientDataJsonDigest =
        SHA256Digest().let {
          it.update(clientDataJsonBytes, 0, clientDataJsonBytes.size)
          ByteArray(it.digestSize).also { out -> it.doFinal(out, 0) }
        }
      val signedMessageBytes = authenticatorData + clientDataJsonDigest
      val messageHash =
        SHA256Digest().let {
          it.update(signedMessageBytes, 0, signedMessageBytes.size)
          ByteArray(it.digestSize).also { out -> it.doFinal(out, 0) }
        }

      val (r, s) = decodeDerEcdsaSignature(signatureDER)

      val x9ecParams = CustomNamedCurves.getByName("secp256r1")
      val curveParams =
        ECDomainParameters(x9ecParams.curve, x9ecParams.g, x9ecParams.n, x9ecParams.h)
      val possibleKeys = mutableListOf<PasskeyPublicKey>()
      for (i in 0..3) {
        try {
          val recoveredPoint =
            recoverPublicKeyPoint(i, r, s, BigInteger(1, messageHash), curveParams)
          if (recoveredPoint != null) {
            possibleKeys.add(PasskeyPublicKey(recoveredPoint.getEncoded(true)))
          }
        } catch (_: Exception) {}
      }
      return possibleKeys
    } catch (e: GetCredentialException) {
      throw IllegalStateException("Signing and recovery failed: ${e.message}", e)
    }
  }

  companion object {
    @Suppress("DEPRECATION")
    private fun applicationOrigin(context: Context): String {
      val packageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES,
          )
        } else {
          context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNATURES,
          )
        }
      val certificate =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          packageInfo.signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
          packageInfo.signatures?.firstOrNull()
        }
          ?: throw IllegalStateException("Unable to determine the Android application origin")
      val certificateHash = MessageDigest.getInstance("SHA-256").digest(certificate.toByteArray())
      val encoded =
        android.util.Base64.encodeToString(
          certificateHash,
          android.util.Base64.URL_SAFE or
            android.util.Base64.NO_PADDING or
            android.util.Base64.NO_WRAP,
        )
      return "android:apk-key-hash:$encoded"
    }
  }
}
