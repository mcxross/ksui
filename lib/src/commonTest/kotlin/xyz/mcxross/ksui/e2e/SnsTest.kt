package xyz.mcxross.ksui.e2e

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.util.runBlocking

class SnsTest :
  StringSpec({
    val sui = TestResources.sui
    val alice = TestResources.alice

    "Resolve name service names returns Ok on testnet" {
      runBlocking {
        when (val result = sui.resolveNameServiceNames(alice.address, limit = 5, cursor = null)) {
          is Result.Ok -> {
            result.value shouldBe result.value
          }
          is Result.Err -> {
            fail("Failed to resolve SNS names")
          }
        }
      }
    }
  })
