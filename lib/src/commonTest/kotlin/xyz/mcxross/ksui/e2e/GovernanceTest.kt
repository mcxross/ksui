package xyz.mcxross.ksui.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.util.runBlocking

class GovernanceTest :
  StringSpec({
    "Get committee info" {
      runBlocking {
        val resp = sui.getCommitteeInfo().expect { "Failed to get committee info" }
        val data = requireNotNull(resp)
        val epoch = requireNotNull(data.epoch)
        (epoch.epochId.toString().toLong() > 0) shouldBe true
        val validatorSet = requireNotNull(epoch.validatorSet)
        val activeValidators = requireNotNull(validatorSet.activeValidators)
        activeValidators.nodes.isNotEmpty() shouldBe true
      }
    }

    "Get validator APY" {
      runBlocking {
        val resp = sui.getValidatorApy().expect { "Failed to get validator APY" }
        val data = requireNotNull(resp)
        val epoch = requireNotNull(data.epoch)
        (epoch.epochId.toString().toLong() > 0) shouldBe true
      }
    }
  })
