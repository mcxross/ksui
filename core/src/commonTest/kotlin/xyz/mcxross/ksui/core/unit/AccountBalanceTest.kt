package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.assertions.throwables.shouldThrow
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.FundsWithdrawalArg
import xyz.mcxross.ksui.core.model.FundsWithdrawalSource
import xyz.mcxross.ksui.core.model.Holdings
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.ptb

class AccountBalanceTest : StringSpec({
  "holdings keep coin objects and address balance independent" {
    val holdings = Holdings("0x2::sui::SUI", 7uL, 5uL)
    holdings.coinObjects shouldBe 7uL
    holdings.addressBalance shouldBe 5uL
    holdings.total shouldBe 12uL
    shouldThrow<IllegalArgumentException> {
      Holdings("0x2::sui::SUI", ULong.MAX_VALUE, 1uL).total
    }
  }

  "funds withdrawal matches Sui BCS variant layout" {
    val arg = CallArg.FundsWithdrawal(FundsWithdrawalArg(5uL, TypeTag.U64))
    val bytes = Bcs.encodeToByteArray<CallArg>(arg)
    bytes.toList() shouldBe listOf<Byte>(2, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0)
    Bcs.decodeFromByteArray<CallArg>(bytes) shouldBe arg
  }

  "withdrawals remain distinct inputs even when amount and type match" {
    val builder = ProgrammableTransactionBuilder()
    builder.withdrawal(5uL) shouldBe Argument.Input(0u)
    builder.withdrawal(5uL) shouldBe Argument.Input(1u)
    builder.build().inputs.size shouldBe 2
    shouldThrow<IllegalArgumentException> { builder.withdrawal(0uL) }
  }

  "withdrawal preserves amounts above signed Long range" {
    val arg = CallArg.FundsWithdrawal(FundsWithdrawalArg(ULong.MAX_VALUE, TypeTag.U64))
    Bcs.decodeFromByteArray<CallArg>(Bcs.encodeToByteArray<CallArg>(arg)) shouldBe arg
  }

  "sponsor withdrawal serializes with the sponsor source" {
    val arg = CallArg.FundsWithdrawal(
      FundsWithdrawalArg(7uL, TypeTag.U64, FundsWithdrawalSource.Sponsor)
    )
    Bcs.decodeFromByteArray<CallArg>(Bcs.encodeToByteArray<CallArg>(arg)) shouldBe arg
    Bcs.encodeToByteArray<CallArg>(arg).last() shouldBe 1.toByte()
  }

  "transfer balance redeems then sends to recipient address balance" {
    val recipient = AccountAddress.fromString("0x123")
    val transaction = ptb { transferBalance(10uL, recipient) }
    transaction.inputs[0].shouldBeInstanceOf<CallArg.FundsWithdrawal>()
    transaction.inputs[1].shouldBeInstanceOf<CallArg.Pure>()
    transaction.commands.size shouldBe 2
    val redeem = transaction.commands[0].shouldBeInstanceOf<Command.MoveCall>().moveCall
    redeem.module shouldBe "balance"
    redeem.function shouldBe "redeem_funds"
    redeem.arguments shouldBe listOf(Argument.Input(0u))
    val send = transaction.commands[1].shouldBeInstanceOf<Command.MoveCall>().moveCall
    send.module shouldBe "balance"
    send.function shouldBe "send_funds"
    send.arguments shouldBe listOf(Argument.Result(0u), Argument.Input(1u))
    Bcs.decodeFromByteArray<ProgrammableTransaction>(
      Bcs.encodeToByteArray<ProgrammableTransaction>(transaction)
    ) shouldBe transaction
  }

  "withdrawCoin redeems a coin rather than a balance" {
    val transaction = ptb { withdrawCoin(10uL) }
    val redeem = transaction.commands.single().shouldBeInstanceOf<Command.MoveCall>().moveCall
    redeem.module shouldBe "coin"
    redeem.function shouldBe "redeem_funds"
  }
})
