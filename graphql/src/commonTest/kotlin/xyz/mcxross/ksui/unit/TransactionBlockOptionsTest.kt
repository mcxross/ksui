package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.generated.type.TransactionKindInput
import xyz.mcxross.ksui.model.TransactionBlockKindInput

class TransactionBlockOptionsTest :
  StringSpec({
    "TransactionBlockKindInput maps to and from generated types" {
      val generated = TransactionBlockKindInput.PROGRAMMABLE_TX.toGenerated()
      generated shouldBe TransactionKindInput.safeValueOf("PROGRAMMABLE_TX")

      val mapped =
        TransactionBlockKindInput.fromGenerated(TransactionKindInput.safeValueOf("SYSTEM_TX"))
      mapped shouldBe TransactionBlockKindInput.SYSTEM_TX
    }

    "TransactionBlockKindInput maps unknown generated values to UNKNOWN__" {
      val mapped =
        TransactionBlockKindInput.fromGenerated(TransactionKindInput.safeValueOf("UNKNOWN_VALUE"))
      mapped shouldBe TransactionBlockKindInput.UNKNOWN__
    }
  })
