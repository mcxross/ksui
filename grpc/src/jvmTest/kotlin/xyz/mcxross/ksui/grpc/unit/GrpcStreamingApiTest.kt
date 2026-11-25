package xyz.mcxross.ksui.grpc.unit

import com.google.protobuf.kotlin.FieldMaskInternal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import sui.rpc.v2.EventFilterInternal
import sui.rpc.v2.ListCheckpointsRequestInternal
import sui.rpc.v2.ListEventsRequestInternal
import sui.rpc.v2.QueryEndReason
import sui.rpc.v2.SubscribeEventsRequest
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.TransactionFilterInternal
import sui.rpc.v2.presence
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.grpc.eventFilter
import xyz.mcxross.ksui.grpc.transactionFilter

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

    "the README filter examples build the documented filters" {
      // Keep in sync with the "Streaming" section of README.md.
      val me = "0x5"
      val nftId = "0x6"
      val myBot = "0x7"
      val filter = transactionFilter {
        anyOf {
          allOf {
            affectedAddress(me)
            moveCall("0x2::coin")
          }
          affectedObject(nftId)
        }
      }
      filter.terms.map { it.literals.size } shouldBe listOf(2, 1)
      val stakes = eventFilter {
        eventType("0x3::validator::StakingRequestEvent")
        not { sender(myBot) }
      }
      stakes.terms.single().literals.map { it.negated } shouldBe listOf(false, true)
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
