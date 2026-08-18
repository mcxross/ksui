package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.GasData
import xyz.mcxross.ksui.core.model.GasLessTransactionData
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.SponsoredTransactionPolicy
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.model.validateSponsoredTransaction
import xyz.mcxross.ksui.core.ptb.TransactionKind
import xyz.mcxross.ksui.core.ptb.ptb
import xyz.mcxross.ksui.core.util.fromBase64

class TransactionDataTest :
  StringSpec({
    "Restore Base64 Envelope" {
      val base64 =
        "AQAAAAAABwEAZ6YDUpCZh8Cjd30VRE6INZnuh5nHQqqXtqIyBdophnpU52ApAAAAACA3cOl7frG7eApDMDIem+i31XjdrKpAEs+fbhFdB+NNYQALCjFMT1pvWXBrUHUADAtkZXNjcmlwdGlvbgAJAQAAAAAAAAAAAAiguw0AAAAAAAEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAYBAAAAAAAAAAAAQQJ6rsGiTO1PNNScJ/ALIfXjx6myDyXleh/ShjsVq+OpBHquwaJM7U801Jwn8Ash9ePHqbIPJeV6H9KGOxWr46kCBACcCdr1mwYwdipxKp3QQ+s1zsh9Xdu3dFJJe92HOSubUAhwMnBfcmFtcAxhdXRoZW50aWNhdGUAAQEAAAAQyHwp6l1WdEWGUqurokZ0KnY/ner+0RYIt/C66ilkhAdpbnRlbnRzCm5ld19wYXJhbXMABQEBAAECAAEDAAEEAAEFAACcCdr1mwYwdipxKp3QQ+s1zsh9Xdu3dFJJe92HOSubUAhwMnBfcmFtcBZlbXB0eV9hcHByb3ZlZF9vdXRjb21lAAAAnAna9ZsGMHYqcSqd0EPrNc7IfV3bt3RSSXvdhzkrm1AGY29uZmlnF3JlcXVlc3RfY29uZmlnX3AycF9yYW1wAAUCAAACAQACAgABAAABBgB6rsGiTO1PNNScJ/ALIfXjx6myDyXleh/ShjsVq+OpBAHroNwNwu/AbXQJqV5v4ArHUolnwjQxjzf5a72EyknD11TnYCkAAAAAIAroFm5teT7w/i3c0A4E5GZmmr+PFNLYGqAWGV0B5P0Yeq7BokztTzTUnCfwCyH148epsg8l5Xof0oY7FavjqQToAwAAAAAAAMDh5AAAAAAAAAFhACfB5kMSH7cIEThY5b1zV+dSHXn7laOCgWa7gT8VFEZ1XMawGgu5RNyy6aKlV1EWV5WOBaWZuEaTufaMoRXccgKGCqQKA5cdaGA6Nc0HniyXvTTmMZZRwNbIsGppr9/iUQ=="

      val txData = TransactionData.fromBase64(base64)

      txData.shouldBeInstanceOf<TransactionData.V1>()

      txData.kind.shouldBeInstanceOf<TransactionKind.ProgrammableTransaction>()
    }

    "Sponsored transaction validation accepts only the requested intent and bounded sponsor gas" {
      val sender = AccountAddress.fromString("0x123")
      val sponsor = AccountAddress.fromString("0x456")
      val requested = GasLessTransactionData.new(ptb { pure(100UL) }, sender)
      val payment =
        ObjectReference(
          reference = Reference(sponsor),
          version = 1,
          digest = ObjectDigest(Digest(ByteArray(32) { 1 })),
        )
      val policy =
        SponsoredTransactionPolicy(
          maxGasBudget = 2_000_000UL,
          maxGasPrice = 2_000UL,
          expectedSponsor = sponsor,
        )
      val valid =
        TransactionData.V1(
          kind = requested.kind,
          sender = requested.sender,
          gasData =
            GasData(
              payment = listOf(payment),
              owner = sponsor,
              price = 1_000UL,
              budget = 1_000_000UL,
            ),
          expiration = requested.expiration,
        )

      requested.validateSponsoredTransaction(valid, policy) shouldBe valid

      val substituted =
        valid.copy(kind = GasLessTransactionData.new(ptb { pure(200UL) }, sender).kind)
      runCatching { requested.validateSponsoredTransaction(substituted, policy) }
        .exceptionOrNull()
        .shouldBeInstanceOf<IllegalArgumentException>()

      val excessiveGas = valid.copy(gasData = valid.gasData.copy(budget = 2_000_001UL))
      runCatching { requested.validateSponsoredTransaction(excessiveGas, policy) }
        .exceptionOrNull()
        .shouldBeInstanceOf<IllegalArgumentException>()
    }
  })
