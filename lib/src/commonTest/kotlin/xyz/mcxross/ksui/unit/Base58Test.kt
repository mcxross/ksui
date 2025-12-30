package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.util.decodeBase58
import xyz.mcxross.ksui.util.encodeToBase58String

class Base58Test :
  StringSpec({
    "Base58 round-trips with leading zeros" {
      val original = byteArrayOf(0, 0, 1, 2, 3, 4, 5)
      val encoded = original.encodeToBase58String()
      val decoded = encoded.decodeBase58()

      decoded shouldBe original
    }

    "Base58 decoding rejects invalid characters" {
      shouldThrow<NumberFormatException> { "0O0".decodeBase58() }
    }
  })
