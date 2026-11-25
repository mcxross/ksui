package xyz.mcxross.ksui.grpc.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeStrictlyIncreasing
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import kotlinx.rpc.grpc.GrpcStatusCode
import sui.rpc.v2.EventFilterInternal
import sui.rpc.v2.ListTransactionsRequest
import sui.rpc.v2.TransactionFilterInternal
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.grpc.StreamFrame
import xyz.mcxross.ksui.grpc.StreamPosition
import xyz.mcxross.ksui.grpc.StreamRetryPolicy

private val FAST_RETRY =
  StreamRetryPolicy(initialDelay = 5.milliseconds, maxDelay = 20.milliseconds, ledgerTipPollInterval = 5.milliseconds)

private fun <T : Any> Flow<Result<StreamFrame<T>, SuiError>>.frames(n: Int): List<StreamFrame<T>> {
  var out: List<Result<StreamFrame<T>, SuiError>> = emptyList()
  runBlocking { out = withTimeout(20.seconds) { take(n).toList() } }
  return out.map { it.shouldBeInstanceOf<Result.Ok<StreamFrame<T>>>().value }
}

private val List<StreamFrame<sui.rpc.v2.Checkpoint>>.numbers get() = mapNotNull { it.item?.sequenceNumber }

class ResumableStreamTest :
  StringSpec({
    "a fresh checkpoint stream starts at the live tip without replaying" {
      val ledger = FakeLedger(tip = 100u)
      val frames = ledger.withClient { it.checkpointStream(retry = FAST_RETRY).frames(5) }
      frames.numbers shouldBe (100uL..104uL).toList()
      frames.none { it.replayed } shouldBe true
      frames.last().resumeFrom shouldBe StreamPosition(105u)
    }

    "reconnects after disconnects and replays the checkpoints it missed, without gaps or duplicates" {
      val ledger =
        FakeLedger(tip = 100u).apply {
          failSubscriptionsAfterFrames = 4
          checkpointsBetweenSubscriptions = 6u
        }
      val frames = ledger.withClient { it.checkpointStream(retry = FAST_RETRY).frames(40) }
      frames.numbers shouldBe (100uL until 140uL).toList()
      frames.any { it.replayed } shouldBe true
      ledger.subscriptions.get() shouldBeGreaterThan 3
    }

    "starting from an earlier position replays up to the live tip first" {
      val ledger = FakeLedger(tip = 100u)
      val frames =
        ledger.withClient { it.checkpointStream(resumeFrom = StreamPosition(90u), retry = FAST_RETRY).frames(15) }
      frames.numbers shouldBe (90uL until 105uL).toList()
      frames.take(10).all { it.replayed } shouldBe true
      frames.drop(10).none { it.replayed } shouldBe true
    }

    "a replay waits while the List index trails the live stream, and pages through item limits" {
      val ledger =
        FakeLedger(tip = 100u, indexedTip = 80u, listPageSize = 3).apply { indexCatchUpPerScan = 2u }
      val frames =
        ledger.withClient { it.checkpointStream(resumeFrom = StreamPosition(80u), retry = FAST_RETRY).frames(25) }
      frames.numbers shouldBe (80uL until 105uL).toList()
      ledger.scans.get() shouldBeGreaterThan 7
    }

    "a saved resume point continues exactly after the last handled frame" {
      val ledger = FakeLedger(tip = 100u)
      val (first, second) =
        ledger.withClient { client ->
          val first = client.checkpointStream(retry = FAST_RETRY).frames(3)
          ledger.tip += 5u
          first to client.checkpointStream(resumeFrom = first.last().resumeFrom, retry = FAST_RETRY).frames(10)
        }
      first.numbers shouldBe listOf(100uL, 101uL, 102uL)
      second.numbers shouldBe (103uL until 113uL).toList()
    }

    "filtered checkpoint streams send progress frames between matches and resume through them" {
      val ledger =
        FakeLedger(tip = 100u).apply {
          failSubscriptionsAfterFrames = 5
          checkpointsBetweenSubscriptions = 4u
        }
      val frames =
        ledger.withClient { it.checkpointStream(filter = TransactionFilterInternal(), retry = FAST_RETRY).frames(40) }
      frames.numbers.all { it % 3u == 0uL } shouldBe true
      frames.numbers.shouldBeStrictlyIncreasing()
      frames.numbers.zipWithNext().all { (a, b) -> b - a == 3uL } shouldBe true
      frames.any { it.item == null } shouldBe true
      frames.map { it.resumeFrom }.zipWithNext().all { (a, b) -> a < b } shouldBe true
    }

    "transaction streams resume by watermark across disconnects without gaps or duplicates" {
      val ledger =
        FakeLedger(tip = 100u).apply {
          failSubscriptionsAfterFrames = 5
          checkpointsBetweenSubscriptions = 3u
        }
      val frames = ledger.withClient { it.transactionStream(retry = FAST_RETRY).frames(60) }
      val keys = frames.mapNotNull { f -> f.item?.let { Key(it.checkpoint!!, it.transactionIndex!!) } }
      keys.first() shouldBe Key(100u, 0u)
      keys.zipWithNext().all { (a, b) -> b == if (a.t == 1uL) Key(a.c + 1u, 0u) else Key(a.c, a.t + 1u) } shouldBe true
      ledger.requests.filterIsInstance<ListTransactionsRequest>().any { it.options.after != null } shouldBe true
    }

    "filtered event streams resume across disconnects with unique, ordered events" {
      val ledger =
        FakeLedger(tip = 100u).apply {
          failSubscriptionsAfterFrames = 4
          checkpointsBetweenSubscriptions = 2u
        }
      val frames = ledger.withClient { it.eventStream(filter = EventFilterInternal(), retry = FAST_RETRY).frames(40) }
      val keys = frames.mapNotNull { f -> f.item?.let { Key(it.checkpoint!!, it.transactionIndex!!, it.eventIndex!!) } }
      keys.all { it.e == 0u } shouldBe true
      keys.shouldBeStrictlyIncreasing()
      keys.zipWithNext().all { (a, b) -> b == if (a.t == 1uL) Key(a.c + 1u, 0u, 0u) else Key(a.c, a.t + 1u, 0u) } shouldBe true
    }

    "a non-retryable status ends the stream with an error" {
      val ledger = FakeLedger().apply { subscribeError = GrpcStatusCode.INVALID_ARGUMENT }
      val results = ledger.withClient { client ->
        var out: List<Result<StreamFrame<sui.rpc.v2.Checkpoint>, SuiError>> = emptyList()
        runBlocking { out = withTimeout(10.seconds) { client.checkpointStream(retry = FAST_RETRY).toList() } }
        out
      }
      results.single().shouldBeInstanceOf<Result.Err<SuiError>>().error.errors!!.single().extensions!!["code"] shouldBe
        "INVALID_ARGUMENT"
      ledger.subscriptions.get() shouldBe 0
    }

    "StreamRetryPolicy.NONE gives up on the first disconnect" {
      val ledger = FakeLedger().apply { failSubscriptionsAfterFrames = 2 }
      val results = ledger.withClient { client ->
        var out: List<Result<StreamFrame<sui.rpc.v2.Checkpoint>, SuiError>> = emptyList()
        runBlocking { out = withTimeout(10.seconds) { client.checkpointStream(retry = StreamRetryPolicy.NONE).toList() } }
        out
      }
      results.dropLast(1).size shouldBe 2
      results.last().shouldBeInstanceOf<Result.Err<SuiError>>().error.errors!!.single().extensions!!["code"] shouldBe
        "UNAVAILABLE"
    }
  })
