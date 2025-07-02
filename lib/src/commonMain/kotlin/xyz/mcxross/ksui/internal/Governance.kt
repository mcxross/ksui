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
import xyz.mcxross.ksui.generated.GetCommitteeInfoQuery
import xyz.mcxross.ksui.generated.GetStakesByIdsQuery
import xyz.mcxross.ksui.generated.GetStakesQuery
import xyz.mcxross.ksui.generated.GetValidatorsApyQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getCommitteeInfo(
  config: SuiConfig,
  epochId: Long?,
  after: String?,
): Result<GetCommitteeInfoQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetCommitteeInfoQuery(
            Optional.presentIfNotNull(epochId),
            Optional.presentIfNotNull(after),
          )
        )
    }
    .toResult()

internal suspend fun getStakes(
  config: SuiConfig,
  address: AccountAddress,
  limit: Int?,
  cursor: String?,
): Result<GetStakesQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetStakesQuery(
            address.toString(),
            Optional.presentIfNotNull(limit),
            Optional.presentIfNotNull(cursor),
          )
        )
    }
    .toResult()

internal suspend fun getStakesByIds(
  config: SuiConfig,
  ids: List<String>,
  limit: Int?,
  cursor: String?,
): Result<GetStakesByIdsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetStakesByIdsQuery(
            ids = ids,
            limit = Optional.presentIfNotNull(limit),
            cursor = Optional.presentIfNotNull(cursor),
          )
        )
    }
    .toResult()

internal suspend fun getValidatorsApy(
  config: SuiConfig
): Result<GetValidatorsApyQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetValidatorsApyQuery()) }.toResult()
