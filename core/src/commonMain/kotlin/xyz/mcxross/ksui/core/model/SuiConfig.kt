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
package xyz.mcxross.ksui.core.model

import kotlin.concurrent.Volatile
import xyz.mcxross.ksui.core.client.ClientConfig
import xyz.mcxross.ksui.core.util.NetworkToFaucetAPI
import xyz.mcxross.ksui.core.util.NetworkToIndexerAPI
import xyz.mcxross.ksui.core.util.NetworkToNodeAPI

class SuiConfig(settings: SuiSettings? = null) : AutoCloseable {

  @Volatile var clientResource: AutoCloseable? = null

  val network: Network = settings?.network ?: Network.DEVNET
  val fullNode: String? = settings?.fullNode
  val faucet: String? = settings?.faucet
  val indexer: String? = settings?.indexer
  val clientConfig: ClientConfig = settings?.clientConfig ?: ClientConfig.default
  val fullNodeConfig: FullNodeConfig = settings?.fullNodeConfig ?: FullNodeConfig()
  val indexerConfig: IndexerConfig = settings?.indexerConfig ?: IndexerConfig()
  val faucetConfig: FaucetConfig = settings?.faucetConfig ?: FaucetConfig()

  override fun close() {
    val resource = clientResource
    clientResource = null
    resource?.close()
  }

  fun getRequestUrl(apiType: SuiApiType): String {
    return when (apiType) {
      SuiApiType.FULLNODE -> {
        fullNode
          ?: if (network == Network.CUSTOM) throw Exception("Please provide a custom full node url")
          else
            NetworkToNodeAPI.getOrElse(network.name.lowercase()) {
              throw Exception("Invalid network")
            }
      }
      SuiApiType.FAUCET -> {
        faucet
          ?: if (network == Network.CUSTOM) throw Exception("Please provide a custom faucet url")
          else
            NetworkToFaucetAPI.getOrElse(network.name.lowercase()) {
              throw Exception("Invalid network")
            }
      }
      SuiApiType.INDEXER -> {
        indexer
          ?: if (network == Network.CUSTOM) throw Exception("Please provide a custom indexer url")
          else
            NetworkToIndexerAPI.getOrElse(network.name.lowercase()) {
              throw Exception("Invalid network")
            }
      }
    }
  }

  fun getHeaders(apiType: SuiApiType): Map<String, Any>? {
    return when (apiType) {
      SuiApiType.FULLNODE -> fullNodeConfig.headers
      SuiApiType.INDEXER -> indexerConfig.headers
      SuiApiType.FAUCET -> {
        val headers = faucetConfig.headers?.toMutableMap() ?: mutableMapOf()
        faucetConfig.authToken?.let { headers["Authorization"] = "Bearer $it" }
        headers.ifEmpty { null }
      }
    }
  }
}

/** General type definition for client headers */
open class ClientHeadersType {
  open var headers: Map<String, Any>? = null
}

/**
 * A Fullnode only configuration object.
 *
 * @param headers - extra headers we want to send with the request
 */
data class FullNodeConfig(override var headers: Map<String, Any>? = null) : ClientHeadersType() {
  override fun toString(): String = "FullNodeConfig(headers=${redactHeaders(headers)})"
}

/**
 * An Indexer only configuration object.
 *
 * @param headers - extra headers we want to send with the request
 */
data class IndexerConfig(override var headers: Map<String, Any>? = null) : ClientHeadersType() {
  override fun toString(): String = "IndexerConfig(headers=${redactHeaders(headers)})"
}

/**
 * A Faucet only configuration object
 *
 * @param headers - extra headers we want to send with the request
 * @param authToken - an auth token to send with a faucet request
 */
data class FaucetConfig(val headers: Map<String, Any>? = null, val authToken: String? = null) {
  override fun toString(): String =
    "FaucetConfig(headers=${redactHeaders(headers)}, authToken=${if (authToken == null) "null" else "***"})"
}

// Header values often carry API keys, so keep them out of logs and crash reports.
private fun redactHeaders(headers: Map<String, Any>?): String =
  headers?.keys?.joinToString(prefix = "{", postfix = "}") { "$it=***" } ?: "null"
