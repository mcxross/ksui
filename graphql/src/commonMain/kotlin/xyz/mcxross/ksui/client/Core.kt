/*
 * Copyright 2024 McXross
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
package xyz.mcxross.ksui.client

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.api.http.HttpMethod as ApolloHttpMethod
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpEngine
import com.apollographql.apollo.network.ws.GraphQLWsProtocol
import com.apollographql.apollo.network.ws.WebSocketEngine
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod as KtorHttpMethod
import io.ktor.http.contentType
import kotlin.concurrent.Volatile
import okio.Buffer
import xyz.mcxross.ksui.core.client.ClientConfig
import xyz.mcxross.ksui.core.model.SuiApiType
import xyz.mcxross.ksui.core.model.SuiConfig

expect fun httpClient(clientConfig: ClientConfig): HttpClient

internal expect fun <T> withClientInitLock(block: () -> T): T

fun getClient(clientConfig: ClientConfig): HttpClient = httpClient(clientConfig)

fun getClient(config: SuiConfig): HttpClient = sessionFor(config).httpClient

fun getGraphqlClient(config: SuiConfig): ApolloClient = sessionFor(config).graphqlClient

internal fun getSharedGraphqlSubscriptionClient(config: SuiConfig): ApolloClient =
  sessionFor(config).subscriptionClient

internal fun getGraphqlSubscriptionClient(
  config: SuiConfig,
  webSocketEngine: WebSocketEngine? = null,
): ApolloClient = buildSubscriptionApolloClient(config, webSocketEngine)

internal fun installHttpClientForTest(config: SuiConfig, client: HttpClient) {
  withClientInitLock {
    config.close()
    config.clientResource = GraphqlClientSession(config, presetHttp = client)
  }
}

private fun sessionFor(config: SuiConfig): GraphqlClientSession {
  (config.clientResource as? GraphqlClientSession)?.let {
    return it
  }
  return withClientInitLock {
    (config.clientResource as? GraphqlClientSession)
      ?: GraphqlClientSession(config).also { config.clientResource = it }
  }
}

private class GraphqlClientSession(
  private val config: SuiConfig,
  presetHttp: HttpClient? = null,
) : AutoCloseable {
  @Volatile private var closed = false
  @Volatile private var lazyHttp: HttpClient? = presetHttp
  @Volatile private var lazyGraphql: ApolloClient? = null
  @Volatile private var lazySubscription: ApolloClient? = null

  val httpClient: HttpClient
    get() {
      check(!closed) { "Sui client session is closed" }
      lazyHttp?.let {
        return it
      }
      return withClientInitLock {
        check(!closed) { "Sui client session is closed" }
        lazyHttp ?: httpClient(config.clientConfig).also { lazyHttp = it }
      }
    }

  val graphqlClient: ApolloClient
    get() {
      check(!closed) { "Sui client session is closed" }
      lazyGraphql?.let {
        return it
      }
      return withClientInitLock {
        check(!closed) { "Sui client session is closed" }
        lazyGraphql ?: buildHttpApolloClient(config, httpClient).also { lazyGraphql = it }
      }
    }

  val subscriptionClient: ApolloClient
    get() {
      check(!closed) { "Sui client session is closed" }
      lazySubscription?.let {
        return it
      }
      return withClientInitLock {
        check(!closed) { "Sui client session is closed" }
        lazySubscription
          ?: buildSubscriptionApolloClient(config, webSocketEngine = null).also {
            lazySubscription = it
          }
      }
    }

  override fun close() {
    withClientInitLock {
      if (!closed) {
        closed = true
        val gql = lazyGraphql
        val sub = lazySubscription
        val http = lazyHttp
        lazyGraphql = null
        lazySubscription = null
        lazyHttp = null
        gql?.close()
        sub?.close()
        http?.close()
      }
    }
  }
}

internal fun buildHttpApolloClient(
  config: SuiConfig,
  ktorClient: HttpClient,
): ApolloClient {
  val builder =
    ApolloClient.Builder()
      .serverUrl(config.getRequestUrl(SuiApiType.INDEXER))
      .httpEngine(KtorApolloHttpEngine(ktorClient))
  config.getHeaders(SuiApiType.INDEXER)?.forEach { (key, value) ->
    builder.addHttpHeader(key, value.toString())
  }
  return builder.build()
}

private fun buildSubscriptionApolloClient(
  config: SuiConfig,
  webSocketEngine: WebSocketEngine?,
): ApolloClient {
  val url = config.getRequestUrl(SuiApiType.INDEXER)
  val transport =
    WebSocketNetworkTransport.Builder()
      .serverUrl(url)
      .protocol(GraphQLWsProtocol.Factory())
      .idleTimeoutMillis(60_000L)
  webSocketEngine?.let(transport::webSocketEngine)

  val builder = ApolloClient.Builder().serverUrl(url)
  config.getHeaders(SuiApiType.INDEXER)?.forEach { (key, value) ->
    builder.addHttpHeader(key, value.toString())
    transport.addHeader(key, value.toString())
  }
  return builder.subscriptionNetworkTransport(transport.build()).build()
}

private class KtorApolloHttpEngine(private val client: HttpClient) : HttpEngine {
  override suspend fun execute(request: HttpRequest): HttpResponse {
    val bodyBytes =
      request.body?.let { apolloBody ->
        val buffer = Buffer()
        apolloBody.writeTo(buffer)
        buffer.readByteArray()
      }

    val ktorResponse =
      client.request(request.url) {
        method =
          when (request.method) {
            ApolloHttpMethod.Get -> KtorHttpMethod.Get
            ApolloHttpMethod.Post -> KtorHttpMethod.Post
          }
        request.headers.forEach { h ->
          if (
            !h.name.equals(HttpHeaders.ContentType, ignoreCase = true) &&
              !h.name.equals(HttpHeaders.ContentLength, ignoreCase = true)
          ) {
            header(h.name, h.value)
          }
        }
        request.body?.let { apolloBody ->
          contentType(ContentType.parse(apolloBody.contentType))
          if (bodyBytes != null) setBody(bodyBytes)
        }
      }

    val responseBytes: ByteArray = ktorResponse.body()
    val responseHeaders =
      ktorResponse.headers.entries().flatMap { (name, values) ->
        values.map { value -> HttpHeader(name, value) }
      }
    return HttpResponse.Builder(statusCode = ktorResponse.status.value)
      .headers(responseHeaders)
      .body(Buffer().write(responseBytes))
      .build()
  }

  override fun close() {}
}
