package xyz.mcxross.ksui.grpc.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.milliseconds
import sui.rpc.v2.EventFilter
import sui.rpc.v2.EventLiteral
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.TransactionFilter
import sui.rpc.v2.TransactionLiteral
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.grpc.StreamRetryPolicy
import xyz.mcxross.ksui.grpc.eventFilter
import xyz.mcxross.ksui.grpc.transactionFilter

private fun full(short: String) = AccountAddress.fromString(short).toString()

/** Each term as a list of "[!]kind:value" strings, so assertions read like the filter. */
private fun TransactionFilter.describe(): List<List<String>> = terms.map { term ->
  term.literals.map { literal ->
    val body =
      when (val p = literal.predicate) {
        is TransactionLiteral.Predicate.Sender -> "sender:${p.value.address}"
        is TransactionLiteral.Predicate.AffectedAddress -> "affectedAddress:${p.value.address}"
        is TransactionLiteral.Predicate.AffectedObject -> "affectedObject:${p.value.objectId}"
        is TransactionLiteral.Predicate.MoveCall -> "moveCall:${p.value.function}"
        is TransactionLiteral.Predicate.EmitModule -> "emitModule:${p.value.module}"
        is TransactionLiteral.Predicate.EventType -> "eventType:${p.value.eventType}"
        is TransactionLiteral.Predicate.EventStreamHead -> "eventStreamHead:${p.value.streamId}"
        is TransactionLiteral.Predicate.PackageWrite -> "packageWrite"
        null -> "none"
      }
    (if (literal.negated) "!" else "") + body
  }
}

private fun EventFilter.describe(): List<List<String>> = terms.map { term ->
  term.literals.map { literal ->
    val body =
      when (val p = literal.predicate) {
        is EventLiteral.Predicate.Sender -> "sender:${p.value.address}"
        is EventLiteral.Predicate.EmitModule -> "emitModule:${p.value.module}"
        is EventLiteral.Predicate.EventType -> "eventType:${p.value.eventType}"
        is EventLiteral.Predicate.EventStreamHead -> "eventStreamHead:${p.value.streamId}"
        null -> "none"
      }
    (if (literal.negated) "!" else "") + body
  }
}

private fun failure(block: () -> Unit): String =
  runCatching(block).exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>().message!!

class FilterDslTest :
  StringSpec({
    "one condition is one term with one literal, with the address in canonical form" {
      transactionFilter { affectedAddress("0x2") }.describe() shouldBe
        listOf(listOf("affectedAddress:${full("0x2")}"))
    }

    "conditions in the block must all hold" {
      transactionFilter {
        sender("0x5")
        moveCall("0x2::coin::transfer")
        packageWrite()
      }
        .describe() shouldBe
        listOf(
          listOf(
            "sender:${full("0x5")}",
            "moveCall:${full("0x2")}::coin::transfer",
            "packageWrite",
          ),
        )
    }

    "anyOf joins each alternative with the block's other conditions" {
      transactionFilter {
        sender("0x5")
        anyOf {
          moveCall("0x2::coin")
          allOf {
            affectedObject("0x6")
            not { emitModule("0xabc::market") }
          }
        }
      }
        .describe() shouldBe
        listOf(
          listOf("sender:${full("0x5")}", "moveCall:${full("0x2")}::coin"),
          listOf(
            "sender:${full("0x5")}",
            "affectedObject:${full("0x6")}",
            "!emitModule:${full("0xabc")}::market",
          ),
        )
    }

    "two anyOf blocks give every combination of their alternatives" {
      transactionFilter {
        anyOf {
          sender("0x1")
          sender("0x2")
        }
        anyOf {
          moveCall("0x3")
          not { packageWrite() }
        }
      }
        .describe() shouldBe
        listOf(
          listOf("sender:${full("0x1")}", "moveCall:${full("0x3")}"),
          listOf("sender:${full("0x1")}", "!packageWrite"),
          listOf("sender:${full("0x2")}", "moveCall:${full("0x3")}"),
          listOf("sender:${full("0x2")}", "!packageWrite"),
        )
    }

    "AccountAddress and ObjectId overloads give the same filter as strings" {
      val address = AccountAddress.fromString("0x7")
      transactionFilter {
        affectedAddress(address)
        affectedObject(xyz.mcxross.ksui.core.model.ObjectId(address))
      }
        .describe() shouldBe
        transactionFilter {
          affectedAddress("0x7")
          affectedObject("0x7")
        }
          .describe()
    }

    "event filters use the event literal type" {
      eventFilter {
        eventType("0x2::coin::CoinEvent")
        not { sender("0x3") }
        anyOf {
          emitModule("0xdee9")
          eventStreamHead("0x9")
        }
      }
        .describe() shouldBe
        listOf(
          listOf(
            "eventType:${full("0x2")}::coin::CoinEvent",
            "!sender:${full("0x3")}",
            "emitModule:${full("0xdee9")}",
          ),
          listOf(
            "eventType:${full("0x2")}::coin::CoinEvent",
            "!sender:${full("0x3")}",
            "eventStreamHead:${full("0x9")}",
          ),
        )
    }

    "empty filters and empty or overfull groups fail when built" {
      failure { transactionFilter {} } shouldContain "at least one condition"
      failure { eventFilter {} } shouldContain "at least one condition"
      failure { transactionFilter { anyOf {} } } shouldContain
        "anyOf { } needs at least one alternative"
      failure { transactionFilter { anyOf { allOf {} } } } shouldContain
        "allOf { } needs at least one condition"
      failure { transactionFilter { not {} } } shouldContain "exactly one condition"
      failure {
        transactionFilter {
          not {
            sender("0x1")
            sender("0x2")
          }
        }
      } shouldContain "exactly one condition"
    }

    "Move paths are checked" {
      transactionFilter { moveCall("0x2") }.describe() shouldBe
        listOf(listOf("moveCall:${full("0x2")}"))
      failure { transactionFilter { moveCall("0x2::coin::transfer::x") } } shouldContain "up to 3"
      failure { transactionFilter { emitModule("0x2::coin::transfer") } } shouldContain "up to 2"
      failure { transactionFilter { moveCall("0x2::1coin") } } shouldContain "not a Move identifier"
      failure { transactionFilter { moveCall("0x2::") } } shouldContain "not a Move identifier"
      failure { transactionFilter { moveCall("coin::transfer") } } shouldContain "not an address"
      failure { transactionFilter { sender("0x-1") } }
    }

    "event types keep their type arguments, in canonical form" {
      val sui = "${full("0x2")}::sui::SUI"
      transactionFilter { eventType("0x2::coin::CoinEvent< 0x2::sui::SUI >") }.describe() shouldBe
        listOf(listOf("eventType:${full("0x2")}::coin::CoinEvent<$sui>"))
      eventFilter {
        eventType("0x3::pool::Swap<vector<0x2::sui::SUI>,u64, 0x2::coin::Coin<0x2::sui::SUI>>")
      }
        .describe() shouldBe
        listOf(
          listOf(
            "eventType:${full("0x3")}::pool::Swap<vector<$sui>, u64, ${full("0x2")}::coin::Coin<$sui>>",
          ),
        )
    }

    "malformed event types fail when built" {
      failure { eventFilter { eventType("0x2::coin<u8>") } } shouldContain "address::module::Name"
      failure { eventFilter { eventType("0x2::m::T<") } }
      failure { eventFilter { eventType("0x2::m::T<u8>>") } } shouldContain "unexpected"
      failure { eventFilter { eventType("0x2::m::T<foo>") } } shouldContain "not an address"
      failure { eventFilter { eventType("0x2::m::T<>") } }
    }

    "a DSL filter reaches the server unchanged through a resumable stream" {
      val ledger = FakeLedger()
      val filter = transactionFilter {
        affectedAddress("0x2")
        anyOf {
          moveCall("0x2::coin")
          not { packageWrite() }
        }
      }
      ledger.withClient { client ->
        client
          .transactionStream(
            filter = filter,
            retry = StreamRetryPolicy(initialDelay = 5.milliseconds, maxDelay = 5.milliseconds),
          )
          .collectOk(3)
      }
      ledger.requests
        .filterIsInstance<SubscribeTransactionsRequest>()
        .single()
        .filter
        .describe() shouldBe filter.describe()
    }
  })
