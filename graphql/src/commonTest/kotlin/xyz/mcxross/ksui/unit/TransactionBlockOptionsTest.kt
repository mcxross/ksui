package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.generated.type.TransactionKindInput
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionBlockKindInput
import xyz.mcxross.ksui.model.transactionBlockResponseOptions

class TransactionBlockOptionsTest :
  StringSpec({
    "transactionBlockResponseOptions builder sets requested fields" {
      val options = transactionBlockResponseOptions {
        first = 10
        after = "cursor"
        showEffects = true
        showEvents = true
      }

      options.first shouldBe 10
      options.after shouldBe "cursor"
      options.showEffects shouldBe true
      options.showEvents shouldBe true
    }

    "ExecuteTransactionBlockResponseOptions defaults to false" {
      val options = ExecuteTransactionBlockResponseOptions()
      options.showEffects shouldBe false
      options.showEvents shouldBe false
      options.showRawInput shouldBe false
    }

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
