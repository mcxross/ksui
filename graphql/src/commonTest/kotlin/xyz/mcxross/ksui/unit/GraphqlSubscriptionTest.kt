/*
 * Copyright 2026 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.mcxross.ksui.unit

import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Subscription
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.network.ws.WebSocketConnection
import com.apollographql.apollo.network.ws.WebSocketEngine
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertContains
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.ByteString
import xyz.mcxross.ksui.client.getGraphqlSubscriptionClient
import xyz.mcxross.ksui.core.model.IndexerConfig
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.generated.SubscribeCheckpointsSubscription
import xyz.mcxross.ksui.generated.SubscribeEventsSubscription
import xyz.mcxross.ksui.generated.SubscribeTransactionsSubscription
import xyz.mcxross.ksui.internal.subscriptionResults
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.TransactionBlockFilter

class GraphqlSubscriptionTest :
  StringSpec({
    "graphql-ws subscriptions send resume variables and parse all three edge types" {
      val config =
        SuiConfig(
          SuiSettings(
            indexer = "https://example.test/graphql",
            indexerConfig = IndexerConfig(mapOf("X-API-Key" to "token")),
          )
        )

      val checkpoint =
        exchange(
          config,
          SubscribeCheckpointsSubscription(
            after = Optional.present("prior-cp"),
            afterCheckpoint = Optional.present("41"),
          ),
          """{"checkpoints":{"cursor":"cp-42","node":{"sequenceNumber":"42","digest":"digest","timestamp":null}}}""",
        )
      checkpoint.payload["variables"]!!.jsonObject["after"]!!.jsonPrimitive.content shouldBe
        "prior-cp"
      checkpoint.payload["variables"]!!.jsonObject["afterCheckpoint"]!!.jsonPrimitive.content shouldBe
        "41"
      (checkpoint.result as Result.Ok).value?.checkpoints?.cursor shouldBe "cp-42"
      checkpoint.engine.url shouldBe "https://example.test/graphql"
      checkpoint.engine.headers.any { it.name == "X-API-Key" && it.value == "token" } shouldBe true
      checkpoint.engine.headers.any {
        it.name.equals("Sec-WebSocket-Protocol", ignoreCase = true) &&
          it.value.contains("graphql-transport-ws")
      } shouldBe true
      checkpoint.engine.closed shouldBe true

      val event =
        exchange(
          config,
          SubscribeEventsSubscription(
            filter = Optional.present(EventFilter(type = "0x2::coin::Coin").toGenerated()),
            after = Optional.present("prior-event"),
          ),
          """{"events":{"cursor":"event-1","node":{"sequenceNumber":"1","sender":null,"contents":null,"timestamp":null}}}""",
        )
      event.payload["variables"]!!.jsonObject["after"]!!.jsonPrimitive.content shouldBe
        "prior-event"
      event.payload["variables"]!!.jsonObject["filter"]!!.jsonObject["type"]!!.jsonPrimitive.content shouldBe
        "0x2::coin::Coin"
      (event.result as Result.Ok).value?.events?.cursor shouldBe "event-1"

      val transaction =
        exchange(
          config,
          SubscribeTransactionsSubscription(
            filter = Optional.present(TransactionBlockFilter(sentAddress = "0x1").toGenerated()),
            after = Optional.present("prior-tx"),
          ),
          """{"transactions":{"cursor":"tx-1","node":{"digest":"tx-digest","sender":null,"effects":null}}}""",
        )
      transaction.payload["variables"]!!.jsonObject["after"]!!.jsonPrimitive.content shouldBe
        "prior-tx"
      transaction.payload["variables"]!!.jsonObject["filter"]!!.jsonObject["sentAddress"]!!.jsonPrimitive.content shouldBe
        "0x1"
      (transaction.result as Result.Ok).value?.transactions?.node?.digest shouldBe "tx-digest"
    }

    "graphql errors become SuiError values" {
      val error =
        exchange(
          SuiConfig(SuiSettings(indexer = "https://example.test/graphql")),
          SubscribeCheckpointsSubscription(),
          """null""",
          error = "denied",
        )
      (error.result as Result.Err).error.errors?.single()?.message shouldBe "denied"
      error.engine.closed shouldBe true
    }
  })

private data class Exchange<D : Subscription.Data>(
  val result: Result<D?, xyz.mcxross.ksui.core.exception.SuiError>,
  val payload: kotlinx.serialization.json.JsonObject,
  val engine: FakeWebSocketEngine,
)

private suspend fun <D : Subscription.Data> exchange(
  config: SuiConfig,
  operation: Subscription<D>,
  data: String,
  error: String? = null,
): Exchange<D> = coroutineScope {
  val engine = FakeWebSocketEngine()
  val collected =
    async {
      subscriptionResults({ getGraphqlSubscriptionClient(config, engine) }, operation).take(1).toList()
    }
  assertContains(withTimeout(5_000) { engine.sent.receive() }, "connection_init")
  engine.received.send("""{"type":"connection_ack"}""")
  val subscribe = Json.parseToJsonElement(withTimeout(5_000) { engine.sent.receive() }).jsonObject
  subscribe["type"]!!.jsonPrimitive.content shouldBe "subscribe"
  val id = subscribe["id"]!!.jsonPrimitive.content
  val response =
    if (error == null) """{"id":"$id","type":"next","payload":{"data":$data}}"""
    else """{"id":"$id","type":"next","payload":{"errors":[{"message":"$error"}]}}"""
  engine.received.send(response)
  val result = withTimeout(5_000) { collected.await().single() }
  Exchange(result, subscribe["payload"]!!.jsonObject, engine)
}

private class FakeWebSocketEngine : WebSocketEngine {
  val sent = Channel<String>(Channel.UNLIMITED)
  val received = Channel<String>(Channel.UNLIMITED)
  lateinit var url: String
  lateinit var headers: List<HttpHeader>
  var closed = false

  override suspend fun open(url: String, headers: List<HttpHeader>): WebSocketConnection {
    this.url = url
    this.headers = headers
    return object : WebSocketConnection {
      override suspend fun receive(): String = received.receive()

      override fun send(string: String) {
        sent.trySend(string)
      }

      override fun send(data: ByteString) {
        error("Expected text WebSocket frames")
      }

      override fun close() {
        closed = true
        received.close()
      }
    }
  }
}
