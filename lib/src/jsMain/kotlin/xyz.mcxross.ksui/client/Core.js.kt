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

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun httpClient(clientConfig: ClientConfig) =
  HttpClient(Js) {

    // Set the follow redirects and SSL redirects.
    followRedirects = clientConfig.followRedirects

    install(DefaultRequest) {
      // headers { DEFAULT_CLIENT_HEADERS.forEach { (key, value) -> append(key, value) } }
    }

    // Set the user agent. If the user wants to use a like agent, use that instead, otherwise use
    // the user's agent.
    if (clientConfig.likeAgent == null) {
      install(UserAgent) { agent = clientConfig.agent }
    } else {
      when (clientConfig.likeAgent) {
        xyz.mcxross.ksui.model.UserAgent.BROWSER -> BrowserUserAgent()
        xyz.mcxross.ksui.model.UserAgent.CURL -> CurlUserAgent()
        else -> {
          install(UserAgent) { agent = clientConfig.agent }
        }
      }
    }

    // Set the timeouts.
    install(HttpTimeout) { requestTimeoutMillis = clientConfig.requestTimeout }

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
      // Set the proxy if the user wants it.
      clientConfig.proxy?.let { proxy = ProxyBuilder.http(it) }
    }
  }

actual class ClientConfig(

  /** Specifies whether the client should follow redirects. Default is `true`. */
  var followRedirects: Boolean = true,

  /** Specifies whether the client should follow SSL redirects. Default is `true`. */
  var likeAgent: xyz.mcxross.ksui.model.UserAgent? = null,

  /** Specifies the user agent. Default is `Ksui/Js`. */
  var agent: String = "Ksui/Js",

  /** Specifies the request timeout in milliseconds. Default is `10000`. */
  var requestTimeout: Long = 10000,

  /**
   * Specifies how many times the client should retry on server errors. Default is `-1`, which means
   * no retries.
   */
  var retryOnServerErrors: Int = -1,

  /**
   * Specifies how many times the client should retry on connection errors. Default is `-1`, which
   * means no retries.
   */
  var maxRetries: Int = -1,

  /** Enables or disables caching. Default is `false`. */
  var cache: Boolean = false,

  /** Specifies the proxy to use. Default is `null`. */
  var proxy: String? = null,
) {
  actual companion object {
    actual val default: ClientConfig
      get() = ClientConfig()
  }
}
