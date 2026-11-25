package xyz.mcxross.ksui.core.crypto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Base64
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.model.PasskeyAuthenticator
import xyz.mcxross.ksui.core.model.Result

class PasskeyVerificationTest :
  StringSpec({
    val rpId = "signin.example.test"
    val origin = "android:apk-key-hash:test-application"
    val challenge = ByteArray(32) { it.toByte() }
    val challengeBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge)
    val clientData = """{"type":"webauthn.get","challenge":"$challengeBase64","origin":"$origin"}"""

    fun sha256(bytes: ByteArray): ByteArray =
      SHA256Digest().let {
        it.update(bytes, 0, bytes.size)
        ByteArray(it.digestSize).also { out -> it.doFinal(out, 0) }
      }

    fun authenticatorData(flags: Int, relyingParty: String = rpId): ByteArray =
      MessageDigest.getInstance("SHA-256").digest(relyingParty.toByteArray()) +
        byteArrayOf(flags.toByte()) +
        ByteArray(4)

    "Passkey assertion policy accepts the expected RP, origin, presence, and verification" {
      passkeyAssertionContextIsValid(
        clientDataJson = clientData,
        authenticatorData = authenticatorData(flags = 0x05),
        message = challenge,
        expectedRpId = rpId,
        expectedOrigin = origin,
        requireUserVerification = true,
      ) shouldBe true
    }

    "Passkey assertion policy rejects origin and RP substitution" {
      passkeyAssertionContextIsValid(
        clientDataJson = clientData,
        authenticatorData = authenticatorData(flags = 0x05),
        message = challenge,
        expectedRpId = rpId,
        expectedOrigin = "android:apk-key-hash:attacker",
        requireUserVerification = true,
      ) shouldBe false

      passkeyAssertionContextIsValid(
        clientDataJson = clientData,
        authenticatorData = authenticatorData(flags = 0x05, relyingParty = "attacker.test"),
        message = challenge,
        expectedRpId = rpId,
        expectedOrigin = origin,
        requireUserVerification = true,
      ) shouldBe false
    }

    "Passkey assertion policy rejects missing presence or user verification" {
      passkeyAssertionContextIsValid(
        clientDataJson = clientData,
        authenticatorData = authenticatorData(flags = 0x04),
        message = challenge,
        expectedRpId = rpId,
        expectedOrigin = origin,
        requireUserVerification = true,
      ) shouldBe false

      passkeyAssertionContextIsValid(
        clientDataJson = clientData,
        authenticatorData = authenticatorData(flags = 0x01),
        message = challenge,
        expectedRpId = rpId,
        expectedOrigin = origin,
        requireUserVerification = true,
      ) shouldBe false
    }

    "Bare passkey public keys fail closed without relying-party context" {
      PasskeyPublicKey(ByteArray(33))
        .verify(challenge, byteArrayOf())
        .shouldBeInstanceOf<Result.Err<Exception>>()
    }

    "Passkey proof-of-key verification works on JVM and rejects high-S malleable signatures" {
      val curve = CustomNamedCurves.getByName("secp256r1")
      val domain = ECDomainParameters(curve.curve, curve.g, curve.n, curve.h)
      val d = BigInteger("7", 10)
      val pubBytes = curve.g.multiply(d).getEncoded(true)
      val passkeyPubKey = PasskeyPublicKey(pubBytes)

      val authData = authenticatorData(flags = 0x05)
      val msgHash = sha256(authData + sha256(clientData.toByteArray(Charsets.UTF_8)))

      val signer = ECDSASigner()
      signer.init(true, ECPrivateKeyParameters(d, domain))
      val components = signer.generateSignature(msgHash)
      val r = components[0]
      val rawS = components[1]
      val lowS = if (rawS > curve.n.shiftRight(1)) curve.n.subtract(rawS) else rawS
      val highS = curve.n.subtract(lowS)

      fun toFixed32(v: BigInteger): ByteArray {
        val b = v.toByteArray()
        val u = if (b.size == 33 && b[0] == 0.toByte()) b.copyOfRange(1, 33) else b
        return if (u.size == 32) u else ByteArray(32 - u.size) + u
      }

      fun buildPasskeySig(sVal: BigInteger): ByteArray {
        val userSig = byteArrayOf(0x02) + toFixed32(r) + toFixed32(sVal) + pubBytes
        return byteArrayOf(0x06) +
          Bcs.encodeToByteArray(PasskeyAuthenticator(authData, clientData, userSig))
      }

      passkeyPubKey.verifySuiProofOfKey(challenge, buildPasskeySig(lowS)) shouldBe Result.Ok(true)
      passkeyPubKey.verifySuiProofOfKey(challenge, buildPasskeySig(highS)) shouldBe Result.Ok(false)

      val derWithTrailingByte =
        DERSequence(arrayOf(ASN1Integer(r), ASN1Integer(lowS))).encoded + byteArrayOf(0x00)
      shouldThrow<IllegalArgumentException> { normalizeSignature(derWithTrailingByte) }
    }
  })
