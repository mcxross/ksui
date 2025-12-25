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
package xyz.mcxross.ksui.dsl

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.client.ClientConfig
import xyz.mcxross.ksui.client.httpClient
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.GasLessTransactionData
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.PtbDsl
import xyz.mcxross.ksui.ptb.ptb

class GasStationConfig {
  var url: String = ""
  var headers: MutableMap<String, String> = mutableMapOf()
}

class SponsoredPtbScope(private val sui: Sui) {
  var sender: AccountAddress = AccountAddress.EMPTY
  private var _ptb: ProgrammableTransaction? = null
  private val _gasStationConfig = GasStationConfig()

  fun gasStation(block: GasStationConfig.() -> Unit) {
    _gasStationConfig.block()
  }

  suspend fun ptb(block: PtbDsl.() -> Unit) {
    _ptb = ptb(sui, block)
  }

  fun build(): Triple<AccountAddress, ProgrammableTransaction, GasStationConfig> {
    require(sender != AccountAddress.EMPTY) { "Sender must be set" }
    require(_ptb != null) { "PTB must be defined" }
    require(_gasStationConfig.url.isNotEmpty()) { "Gas Station URL must be set" }
    return Triple(sender, _ptb!!, _gasStationConfig)
  }
}

@OptIn(ExperimentalEncodingApi::class)
suspend inline fun <reified T, reified R> sponsoredTransaction(
  sui: Sui = xyz.mcxross.ksui.SuiKit.client,
  crossinline requestFactory: (String, String) -> T,
  httpClient: io.ktor.client.HttpClient? = null,
  noinline block: suspend SponsoredPtbScope.() -> Unit,
): R {
  val scope = SponsoredPtbScope(sui)
  scope.block()
  val (sender, ptb, config) = scope.build()

  val gasLess = GasLessTransactionData.new(ptb, sender)
  val txBytes = Base64.encode(Bcs.encodeToByteArray(gasLess))

  val client = httpClient ?: httpClient(sui.config.clientConfig)

  val response =
    client.post(config.url) {
      contentType(ContentType.Application.Json)
      config.headers.forEach { (k, v) -> header(k, v) }
      setBody(requestFactory(txBytes, sender.toString()))
    }

  return response.body<R>()
}
