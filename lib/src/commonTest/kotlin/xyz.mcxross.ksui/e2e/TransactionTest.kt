package xyz.mcxross.ksui.e2e

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.util.runBlocking

class TransactionTest {

  @Test
  fun getTotalTransactionBlocksTest() = runBlocking {
    val resp = sui.getTotalTransactionBlocks().expect("Failed to get total transaction blocks")
    assertNotNull(resp, "Failed to get total transaction blocks")
    assertTrue { resp.checkpoint?.networkTotalTransactions.toString().toLong() > 0 }
  }
}
