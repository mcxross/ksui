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
import xyz.mcxross.ksui.generated.GetAllBalancesQuery
import xyz.mcxross.ksui.generated.GetBalanceQuery
import xyz.mcxross.ksui.generated.GetCoinMetadataQuery
import xyz.mcxross.ksui.generated.GetCoinsQuery
import xyz.mcxross.ksui.generated.GetTotalSupplyQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getAllBalances(
  config: SuiConfig,
  address: AccountAddress,
  limit: Int?,
  cursor: String?,
): Result<GetAllBalancesQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetAllBalancesQuery(
            address.toString(),
            limit = Optional.presentIfNotNull(limit),
            cursor = Optional.presentIfNotNull(cursor),
          )
        )
    }
    .toResult()

internal suspend fun getCoins(
  config: SuiConfig,
  address: AccountAddress,
  first: Int? = null,
  cursor: String? = null,
  type: String? = null,
): Result<GetCoinsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetCoinsQuery(
            address.toString(),
            first = Optional.presentIfNotNull(first),
            cursor = Optional.presentIfNotNull(cursor),
            type = Optional.presentIfNotNull(type),
          )
        )
    }
    .toResult()

internal suspend fun getTotalSupply(
  config: SuiConfig,
  type: String,
): Result<GetTotalSupplyQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetTotalSupplyQuery(type)) }.toResult()

internal suspend fun getBalance(
  config: SuiConfig,
  address: AccountAddress,
  coinType: String?,
): Result<GetBalanceQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(GetBalanceQuery(address.toString(), type = Optional.presentIfNotNull(coinType)))
    }
    .toResult()

internal suspend fun getCoinMetadata(
  config: SuiConfig,
  coinType: String,
): Result<GetCoinMetadataQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetCoinMetadataQuery(coinType)) }.toResult()
