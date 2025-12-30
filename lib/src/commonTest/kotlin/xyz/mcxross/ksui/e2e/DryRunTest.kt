package xyz.mcxross.ksui.e2e

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.model.data
import xyz.mcxross.ksui.model.with
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.runBlocking

class DryRunTest :
  StringSpec({
    val sui = TestResources.sui

    "Dry run transaction block succeeds with raw transaction bytes" {
      runBlocking {
        val coin =
          when (val coinsResult = sui.getCoins(TestResources.alice.address)) {
            is Result.Ok -> {
              val data = requireNotNull(coinsResult.value)
              data.address.objects?.nodes?.firstOrNull() ?: fail("No coins available for dry run")
            }
            is Result.Err -> fail("Failed to fetch coins for dry run")
          }

        val objectRef =
          ObjectReference(
            Reference(AccountAddress.fromString(coin.address.toString())),
            requireNotNull(coin.version).toString().toLong(),
            ObjectDigest(Digest.fromString(requireNotNull(coin.digest))),
          )

        val gasPrice =
          when (val gasPriceResult = sui.getReferenceGasPrice()) {
            is Result.Ok -> {
              val data = requireNotNull(gasPriceResult.value)
              requireNotNull(data.epoch?.referenceGasPrice).toString().toULong()
            }
            is Result.Err -> fail("Failed to fetch gas price for dry run")
          }

        val pt =
          ptb(sui) {
            makeMoveVec {
              typeTag = TypeTag.U8
              values = listOf(pure(1.toByte()), pure(2.toByte()))
            }
          }

        val txData =
          TransactionDataComposer.programmable(
            sender = TestResources.alice.address,
            gasPayment = listOf(objectRef),
            pt = pt,
            gasBudget = 1_000_000uL,
            gasPrice = gasPrice,
          )

        val txBytes = (txData with emptyList()).data()

        val dryRunResult =
          sui.dryRunTransactionBlock(
            txBytes,
            ExecuteTransactionBlockResponseOptions(showEffects = true),
          )

        when (dryRunResult) {
          is Result.Ok -> {
            val data = requireNotNull(dryRunResult.value)
            val simulation = data.simulateTransaction
            if (simulation.error == null) {
              requireNotNull(simulation.effects)
            } else {
              simulation.error.isNotBlank() shouldBe true
            }
          }
          is Result.Err -> {
            fail("Dry run failed")
          }
        }
      }
    }
  })
