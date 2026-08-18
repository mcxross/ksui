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
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import xyz.mcxross.ksui.core.client.ClientConfig
import xyz.mcxross.ksui.core.model.UserAgent as KsuiUserAgent

actual fun httpClient(clientConfig: ClientConfig) =
  HttpClient(OkHttp) {
    followRedirects = clientConfig.followRedirects

    install(DefaultRequest) {}

    install(HttpTimeout) {
      requestTimeoutMillis = clientConfig.requestTimeout
      connectTimeoutMillis = clientConfig.connectTimeout
    }

    // Set the user agent. If the user wants to use a like agent, use that instead, otherwise use
    // the user's agent.
    if (clientConfig.likeAgent == null) {
      install(UserAgent) { agent = clientConfig.agent }
    } else {
      when (clientConfig.likeAgent) {
        KsuiUserAgent.BROWSER -> BrowserUserAgent()
        KsuiUserAgent.CURL -> CurlUserAgent()
        else -> {
          install(UserAgent) { agent = clientConfig.agent }
        }
      }
    }

    // Set the content negotiation. This is required for the client to know how to handle JSON.
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    // How about retries? Things can go wrong, so let's retry a few times.
    if (clientConfig.maxRetries > 0 || clientConfig.retryOnServerErrors > 0) {
      install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = clientConfig.retryOnServerErrors)
        maxRetries = clientConfig.maxRetries
        exponentialDelay()
      }
    }

    // Enable caching if the user wants it.
    if (clientConfig.cache) install(HttpCache)

    engine {

      // Set the proxy if the user wants it.
      clientConfig.proxy?.let { proxy = ProxyBuilder.http(it) }

      config {
        retryOnConnectionFailure(clientConfig.followRedirects)
        connectTimeout(clientConfig.connectTimeoutMillis, TimeUnit.MILLISECONDS)
        followRedirects(clientConfig.followSslRedirects)
        readTimeout(clientConfig.readTimeoutMillis, TimeUnit.MILLISECONDS)
        writeTimeout(clientConfig.writeTimeoutMillis, TimeUnit.MILLISECONDS)
      }
    }
  }
