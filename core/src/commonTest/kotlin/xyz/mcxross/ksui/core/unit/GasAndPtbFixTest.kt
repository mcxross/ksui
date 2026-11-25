package xyz.mcxross.ksui.core.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.CoinObject
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.FundingSource
import xyz.mcxross.ksui.core.model.FundsWithdrawalSource
import xyz.mcxross.ksui.core.model.GasConfig
import xyz.mcxross.ksui.core.model.GasContext
import xyz.mcxross.ksui.core.model.GasCostSummary
import xyz.mcxross.ksui.core.model.GasData
import xyz.mcxross.ksui.core.model.GasEnvironment
import xyz.mcxross.ksui.core.model.GasLessTransactionData
import xyz.mcxross.ksui.core.model.Holdings
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.SponsoredTransactionPolicy
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.model.TransactionDataComposer
import xyz.mcxross.ksui.core.model.TransactionExpiration
import xyz.mcxross.ksui.core.model.validateSponsoredTransaction
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionResolver
import xyz.mcxross.ksui.core.ptb.PtbDsl
import xyz.mcxross.ksui.core.ptb.ResolvedObject
import xyz.mcxross.ksui.core.ptb.ResolvedObjectOwner
import xyz.mcxross.ksui.core.ptb.arg
import xyz.mcxross.ksui.core.ptb.hexStringToByteArray
import xyz.mcxross.ksui.core.ptb.ptb
import xyz.mcxross.ksui.core.model.selectTopGasCoins

class GasAndPtbFixTest :
  StringSpec({
    val sender = AccountAddress.fromString("0x111")
    val chainDigest = Digest(ByteArray(32) { 0x42 })

    fun coinRef(index: Int, balance: ULong): CoinObject {
      val addrBytes = ByteArray(32)
      addrBytes[30] = ((index shr 8) and 0xff).toByte()
      addrBytes[31] = (index and 0xff).toByte()
      return CoinObject(
        reference =
          ObjectReference(
            Reference(AccountAddress(addrBytes)),
            1L,
            ObjectDigest(Digest(ByteArray(32) { 1 })),
          ),
        balance = balance,
      )
    }

    "PTB builder deduplicates mixed-case object IDs and resolves Immutable objects" {
      val builder = ProgrammableTransactionBuilder()
      PtbDsl(builder).apply {
        val a = `object`("0xAbCd")
        val b = `object`("0x0000abcd")
        a shouldBe b
      }
      val resolved =
        builder.build(
          object : ProgrammableTransactionResolver {
            override suspend fun mutableInputIndexes(commands: List<Command>): Set<Int> = emptySet()

            override suspend fun resolveObjects(objectIds: List<String>): Map<String, ResolvedObject> =
              objectIds.associateWith {
                ResolvedObject(
                  objectId = it,
                  version = 5L,
                  digest = "BqrFumAb7yjZKKQUApKjzqdgHUZY3XaTNWxNQsvjMSBc",
                  owner = ResolvedObjectOwner.Immutable,
                )
              }
          }
        )
      resolved.inputs.size shouldBe 1
      val obj = resolved.inputs.single().shouldBeInstanceOf<CallArg.Object>()
      obj.arg.shouldBeInstanceOf<ObjectArg.ImmOrOwnedObject>()
    }

    "Extended.arg encodes 0x1::string::String as Pure rather than ObjectStr" {
      val builder = ProgrammableTransactionBuilder()
      val arg = builder.arg("hello")
      arg shouldBe Argument.Input(0u)
      builder.build().inputs.single().shouldBeInstanceOf<CallArg.Pure>()
    }

    "hexStringToByteArray rejects non-hex characters" {
      shouldThrow<IllegalArgumentException> { hexStringToByteArray("0xZZZZ") }
    }

    "Sponsor FundsWithdrawal does not reduce sender address balance during gas selection" {
      val tx = ptb {
        withdrawal(amount = 9_000_000UL, source = FundsWithdrawalSource.Sponsor)
      }
      val env =
        object : GasEnvironment {
          override suspend fun context(owner: AccountAddress) =
            GasContext(
              epoch = 10UL,
              referenceGasPrice = 1_000UL,
              chain = chainDigest,
              holdings = Holdings("0x2::sui::SUI", coinObjects = 0UL, addressBalance = 10_000_000UL),
            )

          override suspend fun coins(owner: AccountAddress) = emptyList<CoinObject>()

          override suspend fun simulate(transaction: TransactionData) =
            GasCostSummary(1_000_000UL, 0UL, 0UL)
        }
      val composed =
        TransactionDataComposer.programmable(sender, tx, GasConfig(), env)
          .shouldBeInstanceOf<TransactionData.V1>()
      composed.gasData.payment shouldBe emptyList()
      composed.expiration.shouldBeInstanceOf<TransactionExpiration.ValidDuring>()
    }

    "selectTopGasCoins retains highest-balance coins when dust exceeds 256 objects" {
      val top = mutableListOf<CoinObject>()
      val dust = (1..300).map { coinRef(it, 1UL) }
      val whale = listOf(coinRef(999, 50_000_000_000UL))
      selectTopGasCoins(top, dust + whale, limit = 256)
      top.size shouldBe 256
      top.any { it.balance == 50_000_000_000UL } shouldBe true
    }

    "GasConfig smashCoins=false bounds coin selection when SplitCoins uses pure amounts" {
      val tx = ptb {
        val split = splitCoins {
          coin = Argument.GasCoin
          into = listOf(pure(2_000_000UL))
        }
        transferObjects {
          objects = split
          to = address("0x222")
        }
      }
      val env =
        object : GasEnvironment {
          override suspend fun context(owner: AccountAddress) =
            GasContext(
              epoch = 12UL,
              referenceGasPrice = 1_000UL,
              chain = chainDigest,
              holdings = Holdings("0x2::sui::SUI", coinObjects = 30_000_000UL, addressBalance = 5_000_000UL),
            )

          override suspend fun coins(owner: AccountAddress) =
            listOf(coinRef(1, 10_000_000UL), coinRef(2, 10_000_000UL), coinRef(3, 10_000_000UL))

          override suspend fun simulate(transaction: TransactionData) =
            GasCostSummary(1_000_000UL, 0UL, 0UL)
        }
      val composed =
        TransactionDataComposer.programmable(sender, tx, GasConfig(smashCoins = false), env)
          .shouldBeInstanceOf<TransactionData.V1>()
      composed.gasData.payment.size shouldBe 1
    }

    "SponsoredTransactionPolicy rejects mismatched chain digest or excessive epoch window" {
      val sponsor = AccountAddress.fromString("0x999")
      val requested = GasLessTransactionData.new(ptb { pure(1UL) }, sender)
      val wrongChain = Digest(ByteArray(32) { 0x07 })
      val policy =
        SponsoredTransactionPolicy(
          maxGasBudget = 5_000_000UL,
          maxGasPrice = 2_000UL,
          expectedSponsor = sponsor,
          expectedChain = chainDigest,
          maxExpirationEpochs = 2UL,
        )
      val response =
        TransactionData.V1(
          kind = requested.kind,
          sender = sender,
          gasData = GasData(emptyList(), sponsor, 1_000UL, 2_000_000UL),
          expiration = TransactionExpiration.ValidDuring(10UL, 11UL, null, null, wrongChain, 1u),
        )
      shouldThrow<IllegalArgumentException> {
        requested.validateSponsoredTransaction(response, policy)
      }

      val wideWindow =
        response.copy(
          expiration = TransactionExpiration.ValidDuring(10UL, 20UL, null, null, chainDigest, 1u)
        )
      shouldThrow<IllegalArgumentException> {
        requested.validateSponsoredTransaction(wideWindow, policy)
      }

      val valid =
        response.copy(
          expiration = TransactionExpiration.ValidDuring(10UL, 11UL, null, null, chainDigest, 1u)
        )
      requested.validateSponsoredTransaction(valid, policy) shouldBe valid
    }
  })
