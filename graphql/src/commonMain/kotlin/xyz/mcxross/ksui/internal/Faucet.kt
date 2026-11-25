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
package xyz.mcxross.ksui.internal

import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import xyz.mcxross.ksui.client.postSuiFaucet
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.FaucetRequest
import xyz.mcxross.ksui.core.model.FaucetResponse
import xyz.mcxross.ksui.core.model.FixedAmountRequest
import xyz.mcxross.ksui.core.model.Option
import xyz.mcxross.ksui.core.model.RequestOptions
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.TransferredGasObject

internal suspend fun requestTestTokens(
  config: SuiConfig,
  accountAddress: AccountAddress,
): Option<List<TransferredGasObject>> {
  return try {
    val response =
      postSuiFaucet(
        RequestOptions.PostSuiRequestOptions(
          suiConfig = config,
          body = FaucetRequest(FixedAmountRequest(accountAddress.toString())),
        )
      )
    if (!response.status.isSuccess()) return Option.None
    val payload = response.body<FaucetResponse>()
    val objects = payload.transferredGasObjects
    if (payload.error != null || objects == null) {
      Option.None
    } else {
      Option.Some(objects)
    }
  } catch (e: CancellationException) {
    throw e
  } catch (_: Exception) {
    Option.None
  }
}
