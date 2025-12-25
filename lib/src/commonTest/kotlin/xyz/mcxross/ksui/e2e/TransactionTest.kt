package xyz.mcxross.ksui.e2e

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.mcxross.ksui.TestResources.alice
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.model.transactionBlockResponseOptions
import xyz.mcxross.ksui.model.transactionFilter
import xyz.mcxross.ksui.util.runBlocking

class TransactionTest {

  val aliceAddr = alice.address.toString()

  @Test
  fun queryTransactions() = runBlocking {
    val result = sui.queryTransactionBlocks()
    val value = result.expect { "Query failed when it should have succeeded" }
    assertTrue { value?.transactions?.nodes?.isEmpty() == false }
  }

  @Test
  fun queryTransactionsAccountExists() = runBlocking {
    val result =
      sui.queryTransactionBlocks(
        filter =
          transactionFilter {
            affectedAddress = aliceAddr
            sentAddress = aliceAddr
          }
      )

    val value = result.expect { "Query failed when it should have succeeded" }

    assertTrue { value?.transactions?.nodes?.isEmpty() == false }
  }

  @Test
  fun queryTransactionsAccountDoesNotExist() = runBlocking {
    val tmpAddr = Account.create().address.toString()

    val result =
      sui.queryTransactionBlocks(
        filter =
          transactionFilter {
            affectedAddress = tmpAddr
            sentAddress = tmpAddr
          }
      )

    val value = result.expect("Query failed when it should have succeeded")

    assertTrue { value?.transactions?.nodes?.isEmpty() ?: true }
  }

  @Test
  fun queryTransactionsWithOptions() = runBlocking {
    val result =
      sui.queryTransactionBlocks(
        filter =
          transactionFilter {
            affectedAddress = aliceAddr
            sentAddress = aliceAddr
          },
        options =
          transactionBlockResponseOptions {
            first = 10
            showEvents = true
            showEffects = true
            showInput = true
            showRawInput = true
            showBalanceChanges = true
            showRawEffects = true
            showObjectChanges = true
          },
      )

    val value = result.expect { "Query failed when it should have succeeded" }

    assertTrue { value?.transactions?.nodes?.size == 10 }
    assertTrue {
      value?.transactions?.nodes?.any { it.rPC_TRANSACTION_FIELDS.effects != null } == true
    }
    assertTrue {
      value?.transactions?.nodes?.any {
        it.rPC_TRANSACTION_FIELDS.effects?.balanceChanges?.nodes?.isEmpty() == false
      } == true
    }
    assertTrue {
      value?.transactions?.nodes?.any {
        it.rPC_TRANSACTION_FIELDS.effects?.objectChanges?.nodes?.isEmpty() == false
      } == true
    }
    assertTrue {
      value?.transactions?.nodes?.any { it.rPC_TRANSACTION_FIELDS.effects?.bcs != null } == true
    }

    // TODO passed addr should at least have been in a tx that emitted and event
    /*assertTrue {
      value?.transactions?.nodes?.any {
        it.rPC_TRANSACTION_FIELDS.effects?.events?.nodes?.isEmpty() == false
      } == true
    }*/
  }

  @Test
  fun getTotalTransactionBlocksTest() = runBlocking {
    val resp = sui.getTotalTransactionBlocks().expect("Failed to get total transaction blocks")
    assertNotNull(resp, "Failed to get total transaction blocks")
    assertTrue { resp.checkpoint?.networkTotalTransactions.toString().toLong() > 0 }
  }
}
