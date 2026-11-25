package xyz.mcxross.ksui.grpc.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.registerService
import sui.rpc.v2.SignatureVerificationService
import sui.rpc.v2.SubscribeCheckpointsRequest
import sui.rpc.v2.SubscribeCheckpointsResponse
import sui.rpc.v2.SubscribeCheckpointsResponseInternal
import sui.rpc.v2.SubscribeEventsRequest
import sui.rpc.v2.SubscribeEventsResponse
import sui.rpc.v2.SubscribeTransactionsRequest
import sui.rpc.v2.SubscribeTransactionsResponse
import sui.rpc.v2.SubscriptionService
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponse
import sui.rpc.v2.VerifySignatureResponseInternal
import xyz.mcxross.ksui.core.client.ClientConfig
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.grpc.SuiGrpcClient

class GrpcDeadlineTest :
  StringSpec({
    fun withSlowServer(block: (port: Int) -> Unit) {
      val port = unusedTcpPort()
      val server =
        GrpcServer(port) {
            services {
              registerService<SignatureVerificationService> { SlowSignatureService() }
              registerService<SubscriptionService> { SlowCheckpointStream() }
            }
          }
          .start()
      try {
        block(port)
      } finally {
        server.shutdown()
        runBlocking { server.awaitTermination(2.seconds) }
      }
    }

    fun SuiGrpcClient.closeAndWait() {
      shutdownNow()
      runBlocking { awaitTermination(2.seconds) }
    }

    "unary calls fail with DEADLINE_EXCEEDED after ClientConfig.requestTimeout" {
      withSlowServer { port ->
        val config =
          SuiConfig(
            SuiSettings(
              network = Network.CUSTOM,
              fullNode = "http://localhost:$port",
              clientConfig = ClientConfig(requestTimeout = 300),
            ),
          )
        val client = SuiGrpcClient.fromConfig(config)
        try {
          val started = TimeSource.Monotonic.markNow()
          var result: Result<VerifySignatureResponse, SuiError>? = null
          runBlocking { result = client.verifySignature(byteArrayOf(0), byteArrayOf(0)) }
          started.elapsedNow().inWholeMilliseconds shouldBeLessThan 5_000L
          val error = result.shouldBeInstanceOf<Result.Err<SuiError>>().error
          error.errors!!.single().extensions!!["code"] shouldBe "DEADLINE_EXCEEDED"
        } finally {
          client.closeAndWait()
        }
      }
    }

    "streaming subscriptions are not cut off by the unary deadline" {
      withSlowServer { port ->
        val client =
          SuiGrpcClient.connect(
            "localhost",
            port,
            usePlaintext = true,
            requestTimeout = 200.milliseconds,
          )
        try {
          var cursors: List<ULong?> = emptyList()
          runBlocking {
            cursors =
              client.subscribeCheckpoints().take(3).toList().map {
                it.shouldBeInstanceOf<Result.Ok<SubscribeCheckpointsResponse>>().value.cursor
              }
          }
          cursors shouldBe listOf(1UL, 2UL, 3UL)
        } finally {
          client.closeAndWait()
        }
      }
    }

    "connect with an explicit requestTimeout applies it" {
      withSlowServer { port ->
        val client =
          SuiGrpcClient.connect(
            "localhost",
            port,
            usePlaintext = true,
            requestTimeout = 200.milliseconds,
          )
        try {
          var result: Result<VerifySignatureResponse, SuiError>? = null
          runBlocking { result = client.verifySignature(byteArrayOf(0), byteArrayOf(0)) }
          val error = result.shouldBeInstanceOf<Result.Err<SuiError>>().error
          error.errors!!.single().extensions!!["code"] shouldBe "DEADLINE_EXCEEDED"
        } finally {
          client.closeAndWait()
        }
      }
    }
  })

private class SlowSignatureService : SignatureVerificationService {
  override suspend fun VerifySignature(message: VerifySignatureRequest): VerifySignatureResponse {
    delay(30.seconds)
    return VerifySignatureResponseInternal().apply { isValid = true }
  }
}

// Emits a frame every 400 ms, so three frames take longer than the 200 ms unary deadline.
private class SlowCheckpointStream : SubscriptionService {
  override fun SubscribeCheckpoints(
    message: SubscribeCheckpointsRequest,
  ): Flow<SubscribeCheckpointsResponse> = flow {
    for (cursor in 1UL..3UL) {
      delay(400.milliseconds)
      emit(SubscribeCheckpointsResponseInternal().apply { this.cursor = cursor })
    }
  }

  override fun SubscribeTransactions(
    message: SubscribeTransactionsRequest,
  ): Flow<SubscribeTransactionsResponse> = flow {}

  override fun SubscribeEvents(message: SubscribeEventsRequest): Flow<SubscribeEventsResponse> =
    flow {}
}
