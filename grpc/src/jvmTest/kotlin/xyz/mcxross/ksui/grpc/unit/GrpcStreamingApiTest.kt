package xyz.mcxross.ksui.grpc.unit

import com.google.protobuf.kotlin.FieldMaskInternal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import sui.rpc.v2.AffectedAddressFilter
import sui.rpc.v2.EventFilterInternal
import sui.rpc.v2.ListCheckpointsRequestInternal
import sui.rpc.v2.ListEventsRequestInternal
import sui.rpc.v2.QueryEndReason
import sui.rpc.v2.SubscribeEventsRequest
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.TransactionFilter
import sui.rpc.v2.TransactionFilterInternal
import sui.rpc.v2.TransactionLiteral
import sui.rpc.v2.TransactionTerm
import sui.rpc.v2.invoke
import sui.rpc.v2.presence
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.grpc.SuiGrpcClient

internal fun <T> Flow<Result<T, SuiError>>.collectOk(n: Int): List<T> {
  var out: List<T> = emptyList()
  runBlocking { out = take(n).toList().map { (it as Result.Ok).value } }
  return out
}

internal fun <T> FakeLedger.withClient(block: (SuiGrpcClient) -> T): T = serve { port ->
  val client = SuiGrpcClient.connect("localhost", port, usePlaintext = true)
  try {
    block(client)
  } finally {
    client.shutdownNow()
    runBlocking { client.awaitTermination(2.seconds) }
  }
}

class GrpcStreamingApiTest :
  StringSpec({
    "subscribeTransactions sends the filter and read mask and opens with a progress frame" {
      val ledger = FakeLedger()
      val frames = ledger.withClient { client ->
        client
          .subscribeTransactions(
            filter = TransactionFilterInternal(),
            readMask = FieldMaskInternal().apply { paths = listOf("digest") },
          )
          .collectOk(3)
      }
      val request = ledger.requests.filterIsInstance<SubscribeTransactionsRequest>().single()
      request.presence.hasFilter shouldBe true
      request.readMask.paths shouldBe listOf("digest")
      frames.first().presence.hasTransaction shouldBe false
      frames.drop(1).first { it.presence.hasTransaction }.transaction.digest shouldBe "tx-100-0"
    }

    "subscribeEvents streams events with a watermark on every frame" {
      val ledger = FakeLedger()
      val frames = ledger.withClient { it.subscribeEvents(filter = EventFilterInternal()).collectOk(3) }
      ledger.requests.filterIsInstance<SubscribeEventsRequest>().single().presence.hasFilter shouldBe true
      frames.all { it.presence.hasWatermark } shouldBe true
      frames.drop(1).map { it.event.checkpoint } shouldBe listOf(100uL, 100uL)
    }

    "listCheckpoints scans [start, end) and ends with CHECKPOINT_BOUND" {
      val frames = FakeLedger().withClient { client ->
        client
          .listCheckpoints(ListCheckpointsRequestInternal().apply { startCheckpoint = 10u; endCheckpoint = 13u })
          .collectOk(10)
      }
      frames.filter { it.presence.hasCheckpoint }.map { it.checkpoint.sequenceNumber } shouldBe listOf(10uL, 11uL, 12uL)
      frames.last().end.reason shouldBe QueryEndReason.CHECKPOINT_BOUND
    }

    "the README filter example builds an affected-address filter" {
      // Keep in sync with the "Streaming" section of README.md.
      val filter = TransactionFilter {
        terms = listOf(
          TransactionTerm {
            literals = listOf(
              TransactionLiteral {
                predicate = TransactionLiteral.Predicate.AffectedAddress(AffectedAddressFilter { address = "0x2" })
              }
            )
          }
        )
      }
      val predicate = filter.terms.single().literals.single().predicate
      (predicate as TransactionLiteral.Predicate.AffectedAddress).value.address shouldBe "0x2"
    }

    "listEvents stops at the indexed tip with LEDGER_TIP" {
      val ledger = FakeLedger(tip = 20u, indexedTip = 20u)
      val frames = ledger.withClient { client ->
        client.listEvents(ListEventsRequestInternal().apply { startCheckpoint = 20u }).collectOk(10)
      }
      frames.count { it.presence.hasEvent } shouldBe 4
      frames.last().end.reason shouldBe QueryEndReason.LEDGER_TIP
      frames.last().watermark.cursorPosition.checkpoint shouldBe 21uL
    }
  })
