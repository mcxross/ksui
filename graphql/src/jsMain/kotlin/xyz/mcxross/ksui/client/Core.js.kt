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
import xyz.mcxross.ksui.core.client.ClientConfig
import xyz.mcxross.ksui.core.model.UserAgent as KsuiUserAgent

internal actual fun <T> withClientInitLock(block: () -> T): T = block()

actual fun httpClient(clientConfig: ClientConfig) =
  HttpClient(Js) {
    followRedirects = clientConfig.followRedirects

    install(DefaultRequest) {}

    if (clientConfig.likeAgent == null) {
      install(UserAgent) { agent = clientConfig.agent }
    } else {
      when (clientConfig.likeAgent) {
        KsuiUserAgent.BROWSER -> BrowserUserAgent()
        KsuiUserAgent.CURL -> CurlUserAgent()
        else -> install(UserAgent) { agent = clientConfig.agent }
      }
    }

    install(HttpTimeout) {
      requestTimeoutMillis = clientConfig.requestTimeout
      connectTimeoutMillis = clientConfig.connectTimeout
    }

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    if (clientConfig.maxRetries > 0 || clientConfig.retryOnServerErrors > 0) {
      install(HttpRequestRetry) {
        if (clientConfig.retryOnServerErrors > 0) {
          retryOnServerErrors(maxRetries = clientConfig.retryOnServerErrors)
        }
        if (clientConfig.maxRetries > 0) {
          maxRetries = clientConfig.maxRetries
        }
        exponentialDelay()
      }
    }

    if (clientConfig.cache) install(HttpCache)

    engine { clientConfig.proxy?.let { proxy = ProxyBuilder.http(it) } }
  }
