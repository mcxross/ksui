package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.exception.ParsingException

class HexTest :
  StringSpec({
    "Hex formats bytes with prefix and without prefix" {
      val hex = Hex(byteArrayOf(0x0f, 0xa0.toByte()))
      hex.toStringWithoutPrefix() shouldBe "0fa0"
      hex.toString() shouldBe "0x0fa0"
    }

    "Hex.fromString parses with or without 0x prefix" {
      Hex.fromString("0x0a").toString() shouldBe "0x0a"
      Hex.fromString("0a").toString() shouldBe "0x0a"
    }

    "Hex.fromString rejects odd-length hex strings" {
      shouldThrow<ParsingException> { Hex.fromString("0x1") }
    }

    "Hex.fromHexInput preserves data" {
      val input = byteArrayOf(1, 2, 3, 4)
      Hex.fromHexInput(input).toByteArray() shouldBe input
    }
  })
