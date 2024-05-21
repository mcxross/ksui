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

import io.ktor.client.request.*
import io.ktor.http.*
import xyz.mcxross.ksui.model.RequestOptions
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiResponse

suspend inline fun <reified V> post(options: RequestOptions.PostRequestOptions<V>): SuiResponse {
  return client.post(options.suiConfig.getRequestUrl(options.type)) {
    contentType(ContentType.Application.Json)
    setBody(options.body)
  }
}

suspend inline fun <reified T> postSuiFaucet(
  options: RequestOptions.PostSuiRequestOptions<T>
): SuiResponse {
  val response =
    post<T>(
      RequestOptions.PostRequestOptions(
        suiConfig = options.suiConfig,
        type = SuiApiType.FAUCET,
        body = options.body,
      )
    )
  return response
}
