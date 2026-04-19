package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.core.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.core.model.transactionBlockResponseOptions

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
  })
