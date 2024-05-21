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

package xyz.mcxross.kaptos.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import xyz.mcxross.kaptos.model.UserAgent
import xyz.mcxross.kaptos.util.DEFAULT_CLIENT_HEADERS

/** Create a new Ktor client with the given configuration. */
actual fun httpClient(clientConfig: ClientConfig) =
  HttpClient(CIO) {

    // Set the follow redirects and SSL redirects.
    followRedirects = clientConfig.followRedirects

    install(DefaultRequest) {
      headers { DEFAULT_CLIENT_HEADERS.forEach { (key, value) -> append(key, value) } }
    }

    // Set the user agent. If the user wants to use a like agent, use that instead, otherwise use
    // the user's agent.
    if (clientConfig.likeAgent == null) {
      install(UserAgent) { agent = clientConfig.agent }
    } else {
      when (clientConfig.likeAgent) {
        UserAgent.BROWSER -> BrowserUserAgent()
        UserAgent.CURL -> CurlUserAgent()
        else -> {
          install(UserAgent) { agent = clientConfig.agent }
        }
      }
    }

    // Set the timeouts.
    install(HttpTimeout) {
      requestTimeoutMillis = clientConfig.requestTimeout
      connectTimeoutMillis = clientConfig.connectTimeout
    }

    // Set the content negotiation. This is required for the client to know how to handle JSON.
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    // How about retries? Things can go wrong, so let's retry a few times.
    install(HttpRequestRetry) {
      retryOnServerErrors(maxRetries = clientConfig.retryOnServerErrors)
      maxRetries = clientConfig.maxRetries
      exponentialDelay()
    }

    // Enable caching if the user wants it.
    if (clientConfig.cache) install(HttpCache)

    engine {
      maxConnectionsCount = clientConfig.maxConnectionsCount

      // Set the proxy if the user wants it.
      clientConfig.proxy?.let { proxy = ProxyBuilder.http(it) }

      endpoint {
        pipelining = clientConfig.pipelining
        maxConnectionsPerRoute = clientConfig.maxConnectionsPerRoute
        pipelineMaxSize = clientConfig.pipelineMaxSize
        keepAliveTime = clientConfig.keepAliveTime
        connectTimeout = clientConfig.connectTimeoutMillis
        connectAttempts = clientConfig.connectAttempts
      }
    }
  }

actual class ClientConfig {

  /** Specifies whether the client should use pipelining. Default is `false`. */
  var pipelining = false

  /** Specifies the maximum size of the pipeline. Default is `20`. */
  var pipelineMaxSize = 20

  /** Specifies whether the client should follow redirects. Default is `true`. */
  var followRedirects = true

  /** Specifies the maximum number of connections per route. Default is `100`. */
  var maxConnectionsPerRoute = 100

  /** Specifies the maximum number of connections used to make requests. Default is `1000`. */
  var maxConnectionsCount = 1000

  /** Specifies the connect timeout in milliseconds. Default is `10000L`. */
  var connectTimeoutMillis = 10000L

  /** Specifies the keep alive time in milliseconds. Default is `5000L`. */
  var keepAliveTime = 5000L

  /** Specifies the number of connect attempts. Default is `5`. */
  var connectAttempts = 5

  /**
   * Specifies how many times the client should retry on server errors. Default is `-1`, which means
   * no retries.
   */
  var retryOnServerErrors = -1

  /**
   * Specifies how many times the client should retry on connection errors. Default is `-1`, which
   * means no retries.
   */
  var maxRetries = -1

  /** Enables or disables caching. Default is `false`. */
  var cache: Boolean = false

  /** Specifies the user agent. Default is `Kaptos`. */
  var agent: String = "Kaptos/JVM"

  /** Use a like agent. If this is set, the `agent` field will be ignored. */
  var likeAgent: UserAgent? = null

  /** Specifies a timeout for a whole HTTP call, from sending a request to receiving a response. */
  var requestTimeout = 10000L

  /** Specifies a timeout for establishing a connection with a node. */
  var connectTimeout = 10000L

  /** Specifies a proxy for the client to use. */
  var proxy: String? = null
}
