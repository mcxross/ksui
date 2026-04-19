package xyz.mcxross.ksui.unit

import com.apollographql.apollo.api.Optional
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.model.TransactionBlockKindInput
import xyz.mcxross.ksui.model.transactionFilter

class TransactionBlockFilterTest :
  StringSpec({
    "transactionFilter builder maps to generated filter with present fields" {
      val filter = transactionFilter {
        afterCheckpoint = "100"
        function = "0x2::coin::transfer"
        kind = TransactionBlockKindInput.PROGRAMMABLE_TX
        affectedAddress = "0x1"
      }

      val generated = filter.toGenerated()
      (generated.afterCheckpoint is Optional.Present) shouldBe true
      (generated.function is Optional.Present) shouldBe true
      (generated.kind is Optional.Present) shouldBe true
      (generated.affectedAddress is Optional.Present) shouldBe true
    }
  })
