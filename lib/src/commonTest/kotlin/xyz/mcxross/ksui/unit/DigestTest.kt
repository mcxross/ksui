package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.util.encodeToBase58String

class DigestTest :
  StringSpec({
    "Digest.fromString accepts base58 strings for 32-byte data" {
      val bytes = ByteArray(32) { it.toByte() }
      val base58 = bytes.encodeToBase58String()

      val digest = Digest.fromString(base58)
      digest.data shouldBe bytes
    }

    "Digest.fromString rejects non-32-byte data" {
      val bytes = ByteArray(31) { 1 }
      val base58 = bytes.encodeToBase58String()

      shouldThrow<IllegalArgumentException> { Digest.fromString(base58) }
    }
  })
