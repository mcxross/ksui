package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.util.FUNCTION
import xyz.mcxross.ksui.util.MODULE
import xyz.mcxross.ksui.util.PACKAGE_ID
import xyz.mcxross.ksui.util.convertBits
import xyz.mcxross.ksui.util.formatSuiDomain
import xyz.mcxross.ksui.util.idToMapped
import xyz.mcxross.ksui.util.idToParts

class HelperTest :
  StringSpec({
    "formatSuiDomain appends suffix when missing" {
      formatSuiDomain("Alice") shouldBe "alice.sui"
      formatSuiDomain("bob.sui") shouldBe "bob.sui"
    }

    "idToParts splits well-formed identifiers" {
      val parts = idToParts("0x2::coin::balance")
      parts shouldBe Triple("0x2", "coin", "balance")
    }

    "idToMapped returns a labeled map" {
      val mapped = idToMapped("0x2::coin::balance")
      mapped[PACKAGE_ID] shouldBe "0x2"
      mapped[MODULE] shouldBe "coin"
      mapped[FUNCTION] shouldBe "balance"
    }

    "idToParts rejects malformed identifiers" {
      shouldThrow<IllegalArgumentException> { idToParts("0x2::coin") }
    }

    "convertBits rejects invalid padding when pad is false" {
      shouldThrow<IllegalArgumentException> { convertBits(byteArrayOf(1), 8, 5, pad = false) }
    }
  })
