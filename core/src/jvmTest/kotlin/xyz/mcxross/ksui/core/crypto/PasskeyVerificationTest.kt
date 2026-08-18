package xyz.mcxross.ksui.core.crypto

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.security.MessageDigest
import java.util.Base64
import xyz.mcxross.ksui.core.model.Result

class PasskeyVerificationTest :
  StringSpec({
    val rpId = "signin.example.test"
    val origin = "android:apk-key-hash:test-application"
    val challenge = ByteArray(32) { it.toByte() }
    val challengeBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(challenge)
    val clientData =
      """{"type":"webauthn.get","challenge":"$challengeBase64","origin":"$origin"}"""

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
  })
