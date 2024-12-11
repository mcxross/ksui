package xyz.mcxross.ksui.e2e

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.util.runBlocking

class FaucetTest {

  private val sui = TestResources.sui
  private val bob = Account.create()

  @Test
  fun requestTestTokensTest() = runBlocking {
    val resp = sui.requestTestTokens(bob.address).expect("Failed to request test tokens")
    assertNotNull(resp, "Failed to request test tokens")
    assertTrue { resp.isNotEmpty() }
    assertTrue { resp[0].amount > 0 }
  }
}
