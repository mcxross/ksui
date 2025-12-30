package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.core.crypto.KeyPair
import xyz.mcxross.ksui.core.crypto.SignatureScheme

class CryptoModelTest :
  StringSpec({
    "SignatureScheme.valueOf returns scheme for known byte" {
      SignatureScheme.valueOf(0x00.toByte()) shouldBe SignatureScheme.ED25519
      SignatureScheme.valueOf(0x06.toByte()) shouldBe SignatureScheme.PASSKEY
    }

    "SignatureScheme.valueOf returns null for unknown byte" {
      SignatureScheme.valueOf(0x7f.toByte()) shouldBe null
    }

    "KeyPair equality compares byte arrays" {
      val a = KeyPair(byteArrayOf(1, 2), byteArrayOf(3, 4))
      val b = KeyPair(byteArrayOf(1, 2), byteArrayOf(3, 4))
      val c = KeyPair(byteArrayOf(9), byteArrayOf(3, 4))

      (a == b) shouldBe true
      (a == c) shouldBe false
    }
  })
