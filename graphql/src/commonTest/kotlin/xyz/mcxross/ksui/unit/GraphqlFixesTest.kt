package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.client.getClient
import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.client.installHttpClientForTest
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.ObjectDataOptions
import xyz.mcxross.ksui.core.model.Option
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.core.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.ptb.ptb

class GraphqlFixesTest :
  StringSpec({
    val json = Json { ignoreUnknownKeys = true }

    fun OutgoingContent.readBytes(): ByteArray =
      when (this) {
        is OutgoingContent.ByteArrayContent -> bytes()
        else -> ByteArray(0)
      }

    fun mockSui(
      handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): Sui {
      val config =
        SuiConfig(
          SuiSettings(
            indexer = "https://indexer.test/graphql",
            faucet = "https://faucet.test/gas",
          ),
        )
      val ktor = HttpClient(MockEngine(handler)) { install(ContentNegotiation) { json(json) } }
      installHttpClientForTest(config, ktor)
      return Sui(config, makeDefault = false)
    }

    "getGraphqlClient and getClient reuse shared instances until Sui.close()" {
      val config = SuiConfig()
      val sui = Sui(config, makeDefault = false)
      val gql1 = getGraphqlClient(config)
      val gql2 = getGraphqlClient(config)
      val http1 = getClient(config)
      val http2 = getClient(config)

      gql1 shouldBeSameInstanceAs gql2
      http1 shouldBeSameInstanceAs http2
      sui.close()
    }

    "Sui(makeDefault = true) updates SuiKit.client to the latest default instance" {
      val devnet = Sui(SuiConfig(SuiSettings(network = Network.DEVNET)), makeDefault = true)
      val mainnet = Sui(SuiConfig(SuiSettings(network = Network.MAINNET)), makeDefault = true)
      SuiKit.client shouldBeSameInstanceAs mainnet
      devnet.close()
      mainnet.close()
    }

    "Sui.dryRunTransactionBlock forwards showBalanceChanges and showObjectChanges without swapping" {
      var capturedVars: kotlinx.serialization.json.JsonObject? = null
      val sui = mockSui { req ->
        val text = req.body.readBytes().decodeToString()
        capturedVars = json.parseToJsonElement(text).jsonObject["variables"]!!.jsonObject
        respond(
          """{"data":{"simulateTransaction":{"effects":{"status":"SUCCESS","executionError":null,"gasEffects":null}}}}""",
          HttpStatusCode.OK,
          headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }

      sui.dryRunTransactionBlock(
        "AA==",
        ExecuteTransactionBlockResponseOptions(
          showBalanceChanges = false,
          showObjectChanges = true,
        ),
      )
      val vars = requireNotNull(capturedVars)
      vars["showBalanceChanges"]!!.jsonPrimitive.boolean shouldBe false
      vars["showObjectChanges"]!!.jsonPrimitive.boolean shouldBe true
      sui.close()
    }

    "Sui.getObject wires showPreviousTransaction and surfaces GraphQL errors alongside data" {
      var capturedShowPrev: Boolean? = null
      val sui = mockSui { req ->
        val text = req.body.readBytes().decodeToString()
        val vars = json.parseToJsonElement(text).jsonObject["variables"]!!.jsonObject
        capturedShowPrev = vars["showPreviousTransaction"]?.jsonPrimitive?.boolean
        respond(
          """{"data":{"object":null},"errors":[{"message":"partial failure"}]}""",
          HttpStatusCode.OK,
          headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }

      val res =
        sui.getObject(
          "0x5",
          ObjectDataOptions(showOwner = false, showPreviousTransaction = true),
        )
      capturedShowPrev shouldBe true
      res.shouldBeInstanceOf<Result.Err<SuiError>>().error.errors?.single()?.message shouldBe
        "partial failure"
      sui.close()
    }

    "Sui.waitForTransaction polls past null transaction payloads and propagates coroutine cancellation" {
      var calls = 0
      val sui = mockSui {
        calls++
        val payload =
          if (calls < 3) """{"data":{"transaction":null}}"""
          else
            """{"data":{"transaction":{"__typename":"Transaction","digest":"tx-final","sender":null,"signatures":[],"effects":null}}}"""
        respond(
          payload,
          HttpStatusCode.OK,
          headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }

      val found =
        sui.waitForTransaction(
          digest = "tx-final",
          options = TransactionBlockResponseOptions(),
          timeout = 5_000L,
          pollInterval = 10L,
        )
      calls shouldBe 3
      found.shouldBeInstanceOf<Result.Ok<*>>()
      sui.close()

      val hangingSui = mockSui {
        respond(
          """{"data":{"transaction":null}}""",
          HttpStatusCode.OK,
          headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }
      var caughtCancellation = false
      coroutineScope {
        val started = CompletableDeferred<Unit>()
        val job = launch {
          try {
            started.complete(Unit)
            hangingSui.waitForTransaction("missing", pollInterval = 50L, timeout = 10_000L)
          } catch (e: CancellationException) {
            caughtCancellation = true
            throw e
          }
        }
        started.await()
        delay(20L)
        job.cancel()
        job.join()
      }
      caughtCancellation shouldBe true
      hangingSui.close()
    }

    "Sui.requestTestTokens parses FaucetResponse into Option.Some" {
      val sui = mockSui {
        respond(
          """{"transferredGasObjects":[{"amount":1000000000,"id":"0xabc","transferTxDigest":"digest1"}],"error":null}""",
          HttpStatusCode.OK,
          headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }
      val tokens = sui.requestTestTokens(AccountAddress.fromString("0x123"))
      val list =
        tokens
          .shouldBeInstanceOf<Option.Some<List<xyz.mcxross.ksui.core.model.TransferredGasObject>>>()
          .value
      list.single().id shouldBe "0xabc"
      sui.close()
    }

    "GraphQL PTB resolver resolves Immutable objects and mixed-case IDs" {
      val normalizedId = "0x" + "abcd".padStart(64, '0')
      val sui = mockSui {
        respond(
          """{"data":{"multiGetObjects":[{"__typename":"Object","objectId":"$normalizedId","version":7,"digest":"BqrFumAb7yjZKKQUApKjzqdgHUZY3XaTNWxNQsvjMSBc","owner":{"__typename":"Immutable"}}]}}""",
          HttpStatusCode.OK,
          headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }
      val resolved = ptb(sui) { `object`("0xAbCd") }
      val obj = resolved.inputs.single().shouldBeInstanceOf<CallArg.Object>()
      obj.arg.shouldBeInstanceOf<ObjectArg.ImmOrOwnedObject>()
      sui.close()
    }
  })
