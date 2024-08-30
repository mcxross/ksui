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

import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.extension.formatAsSuiDomain
import xyz.mcxross.ksui.generated.ResolveNameServiceAddress
import xyz.mcxross.ksui.generated.ResolveNameServiceNames
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Page
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun resolveNameServiceAddress(
  config: SuiConfig,
  domain: String,
): Option<AccountAddress?> {
  val response =
    getGraphqlClient(config)
      .execute(
        ResolveNameServiceAddress(
          ResolveNameServiceAddress.Variables(domain = domain.formatAsSuiDomain())
        )
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!.resolveSuinsAddress?.let { AccountAddress.fromString(it.address) })
}

internal suspend fun resolveNameServiceNames(
  config: SuiConfig,
  address: AccountAddress,
  limit: UInt? = null,
  cursor: String? = null,
): Option<Page?> {
  val response =
    getGraphqlClient(config)
      .execute(
        ResolveNameServiceNames(
          ResolveNameServiceNames.Variables(
            address = address.toString(),
            limit = limit?.toInt(),
            cursor = cursor,
          )
        )
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}
