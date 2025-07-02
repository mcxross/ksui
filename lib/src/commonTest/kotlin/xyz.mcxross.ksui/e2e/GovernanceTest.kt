package xyz.mcxross.ksui.e2e

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.mcxross.ksui.TestResources.alice
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.util.runBlocking

class GovernanceTest {

  @Test
  fun getCommitteeInfoTest() = runBlocking {
    val resp = sui.getCommitteeInfo().expect { "Failed to get committee info" }
    assertNotNull(resp, "Failed to get committee info")
    assertNotNull(resp.epoch, "Current epoch is null")

    assertTrue { resp.epoch.epochId.toString().toInt() > 0 }
    assertTrue { resp.epoch.validatorSet?.activeValidators?.nodes?.isNotEmpty() ?: false }
  }

  @Test
  fun getStakesTest() = runBlocking {
    val resp = sui.getStakes(alice.address).expect { "Failed to get stakes" }
    assertNotNull(resp, "Failed to get stakes")
  }

  @Test
  fun getValidatorApyTest() = runBlocking {
    val resp = sui.getValidatorApy().expect { "Failed to get validator APY" }
    assertNotNull(resp, "Failed to get validator APY")
    assertNotNull(resp.epoch, "Epoch is null")
    assertTrue { resp.epoch.epochId.toString().toInt() > 0 }
  }
}
