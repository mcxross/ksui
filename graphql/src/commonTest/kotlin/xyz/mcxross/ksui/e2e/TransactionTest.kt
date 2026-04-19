package xyz.mcxross.ksui.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources.alice
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.model.transactionBlockResponseOptions
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.model.transactionFilter

class TransactionTest :
  StringSpec({
    val aliceAddr = alice.address.toString()

    "Query transactions" {
      runBlocking {
        val result = sui.queryTransactionBlocks()
        val value = result.expect { "Query failed when it should have succeeded" }
        value?.transactions?.nodes?.isEmpty() shouldBe false
      }
    }

    "Query transactions for existing account" {
      runBlocking {
        val result =
          sui.queryTransactionBlocks(
            filter =
              transactionFilter {
                affectedAddress = aliceAddr
                sentAddress = aliceAddr
              }
          )

        val value = result.expect { "Query failed when it should have succeeded" }

        value?.transactions?.nodes?.isEmpty() shouldBe false
      }
    }

    "Query transactions for non-existent account" {
      runBlocking {
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

        (value?.transactions?.nodes?.isEmpty() ?: true) shouldBe true
      }
    }

    "Query transactions with options" {
      runBlocking {
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

        value?.transactions?.nodes?.size shouldBe 10
        value?.transactions?.nodes?.any { it.rPC_TRANSACTION_FIELDS.effects != null } shouldBe true
        value?.transactions?.nodes?.any {
          it.rPC_TRANSACTION_FIELDS.effects?.balanceChanges?.nodes?.isEmpty() == false
        } shouldBe true
        value?.transactions?.nodes?.any {
          it.rPC_TRANSACTION_FIELDS.effects?.objectChanges?.nodes?.isEmpty() == false
        } shouldBe true
        value?.transactions?.nodes?.any { it.rPC_TRANSACTION_FIELDS.effects?.bcs != null } shouldBe
          true

        // TODO passed addr should at least have been in a tx that emitted and event
        /*assertTrue {
          value?.transactions?.nodes?.any {
            it.rPC_TRANSACTION_FIELDS.effects?.events?.nodes?.isEmpty() == false
          } == true
        }*/
      }
    }

    "Get total transaction blocks" {
      runBlocking {
        val resp = sui.getTotalTransactionBlocks().expect("Failed to get total transaction blocks")
        val data = requireNotNull(resp)
        val checkpoint = requireNotNull(data.checkpoint)
        (requireNotNull(checkpoint.networkTotalTransactions).toString().toLong() > 0) shouldBe true
      }
    }
  })
