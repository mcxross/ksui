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

import com.apollographql.apollo.api.Optional
import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.ResolveNameServiceAddressQuery
import xyz.mcxross.ksui.generated.ResolveNameServiceNamesQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun resolveNameServiceAddress(
  config: SuiConfig,
  domain: String,
): Result<ResolveNameServiceAddressQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(ResolveNameServiceAddressQuery(domain)) }.toResult()

internal suspend fun resolveNameServiceNames(
  config: SuiConfig,
  address: AccountAddress,
  limit: Int? = null,
  cursor: String? = null,
): Result<ResolveNameServiceNamesQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          ResolveNameServiceNamesQuery(
            address.toString(),
            limit = Optional.presentIfNotNull(limit),
            cursor = Optional.presentIfNotNull(cursor),
          )
        )
    }
    .toResult()
