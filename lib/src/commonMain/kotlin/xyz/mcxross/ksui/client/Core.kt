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
import io.ktor.client.*
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

/**
 * Create a new Ktor client with the given configuration.
 *
 * Each client is platform-specific with a different engine. Each engine has its own configuration
 * options.
 */
expect fun httpClient(clientConfig: ClientConfig): HttpClient

expect class ClientConfig {
  companion object {
    val default: ClientConfig
  }
}

fun getClient(clientConfig: ClientConfig) = httpClient(clientConfig)

fun getGraphqlClient(config: SuiConfig) =
  ApolloClient.Builder().serverUrl(config.getRequestUrl(SuiApiType.INDEXER)).build()
