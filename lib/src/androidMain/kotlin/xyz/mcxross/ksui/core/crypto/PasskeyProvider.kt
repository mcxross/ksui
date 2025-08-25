package xyz.mcxross.ksui.core.crypto

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.*
import androidx.credentials.exceptions.*
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID
import kotlinx.serialization.json.Json
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.ec.CustomNamedCurves
import xyz.mcxross.ksui.account.PasskeyAccount
import xyz.mcxross.ksui.data.PublicKeyCredentialRes

actual class PasskeyProvider(private val context: Context, private val rpId: String) {

  private val credentialManager = CredentialManager.create(context)

  @SuppressLint("PublicKeyCredential")
  actual suspend fun create(name: String): PasskeyAccount {
    val challenge = UUID.randomUUID().toString().toByteArray()

    val request =
      CreatePublicKeyCredentialRequest(
        requestJson =
          """
          {
            "rp": { "id": "$rpId", "name": "Sui Passkey dApp" },
            "user": {
              "id": "${UUID.randomUUID()}",
              "name": "$name",
              "displayName": "$name"
            },
            "challenge": "${android.util.Base64.encodeToString(challenge, android.util.Base64.NO_WRAP)}",
            "pubKeyCredParams": [{ "type": "public-key", "alg": -7 }]
          }
          """
            .trimIndent()
      )

    try {
      val result =
        credentialManager.createCredential(context, request) as CreatePublicKeyCredentialResponse
      val spki = SubjectPublicKeyInfo.getInstance(result.registrationResponseJson.toByteArray())
      val point =
        CustomNamedCurves.getByName("secp256r1").curve.decodePoint(spki.publicKeyData.bytes)
      val publicKeyBytes = point.getEncoded(true)

      return PasskeyAccount(PasskeyPublicKey(publicKeyBytes), this)
    } catch (e: CreateCredentialException) {
      throw IllegalStateException("Failed to create Passkey: ${e.message}", e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  actual suspend fun sign(challenge: ByteArray): ByteArray {
    val challengeBase64Url = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge)

    val requestJson =
      """
          {
            "rpId": "$rpId",
            "challenge": "$challengeBase64Url"
          }
      """
        .trimIndent()

    val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(requestJson = requestJson)
    val request = GetCredentialRequest(credentialOptions = listOf(getPublicKeyCredentialOption))

    try {
      val result = credentialManager.getCredential(context, request)
      val publicKeyCredential = result.credential as PublicKeyCredential
      val responseJsonString = publicKeyCredential.authenticationResponseJson

      val credentials = Json.decodeFromString<PublicKeyCredentialRes>(responseJsonString)

      val authenticatorData = Base64.getUrlDecoder().decode(credentials.response.authenticatorData)
      val clientDataJson = Base64.getUrlDecoder().decode(credentials.response.clientDataJSON)
      val signatureDER = Base64.getUrlDecoder().decode(credentials.response.signature)

      val digest = SHA256Digest()
      digest.update(clientDataJson, 0, clientDataJson.size)
      val clientDataHash = ByteArray(digest.digestSize).also { digest.doFinal(it, 0) }

      val bcsSerializer = ByteArrayOutputStream()

      bcsSerializer.write(serializeBcsVector(authenticatorData))

      bcsSerializer.write(serializeBcsVector(clientDataHash))

      bcsSerializer.write(serializeBcsVector(signatureDER))

      val bcsPayload = bcsSerializer.toByteArray()

      val signatureSchemeFlag: Byte = 0x02
      val suiPasskeySignature = ByteArrayOutputStream()
      suiPasskeySignature.write(signatureSchemeFlag.toInt())
      suiPasskeySignature.write(bcsPayload)

      return suiPasskeySignature.toByteArray()
    } catch (e: GetCredentialException) {
      throw IllegalStateException("Signing failed: ${e.message}", e)
    }
  }

  private fun serializeBcsVector(data: ByteArray): ByteArray {
    val out = ByteArrayOutputStream()
    val length = data.size
    var value = length
    while (value != 0) {
      var byte = value and 0x7F
      value = value ushr 7
      if (value != 0) {
        byte = byte or 0x80
      }
      out.write(byte)
    }
    if (length == 0) {
      out.write(0)
    }
    out.write(data)
    return out.toByteArray()
  }
}
