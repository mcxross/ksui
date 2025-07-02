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
package xyz.mcxross.ksui.api

import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.GetCommitteeInfoQuery
import xyz.mcxross.ksui.generated.GetStakesByIdsQuery
import xyz.mcxross.ksui.generated.GetStakesQuery
import xyz.mcxross.ksui.generated.GetValidatorsApyQuery
import xyz.mcxross.ksui.internal.getCommitteeInfo
import xyz.mcxross.ksui.internal.getStakes
import xyz.mcxross.ksui.internal.getStakesByIds
import xyz.mcxross.ksui.internal.getValidatorsApy
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Governance

/**
 * The concrete implementation of the [Governance] interface.
 *
 * @param config The [SuiConfig] object specifying the RPC endpoint and connection settings.
 */
class Governance(val config: SuiConfig) : Governance {

  /**
   * Fetches the committee of validators for a specific epoch.
   *
   * If the epoch ID is not provided, it defaults to the current epoch.
   *
   * @param epochId The epoch number for which to fetch the committee information.
   * @param after An optional cursor for forward pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetCommitteeInfoQuery.Data] object with a list of validators and
   *   a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getCommitteeInfo(
    epochId: Long?,
    after: String?,
  ): Result<GetCommitteeInfoQuery.Data?, SuiError> = getCommitteeInfo(config, epochId, after)

  /**
   * Fetches all `StakedSui` objects owned by a specific address.
   *
   * @param owner The [AccountAddress] that owns the staked objects.
   * @param limit An optional integer to specify the maximum number of stakes to return per page.
   * @param cursor An optional cursor string for pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetStakesQuery.Data] object with a list of stakes and a
   *   pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getStakes(
    owner: AccountAddress,
    limit: Int?,
    cursor: String?,
  ): Result<GetStakesQuery.Data?, SuiError> = getStakes(config, owner, limit, cursor)

  /**
   * Fetches a list of `StakedSui` objects by their unique IDs.
   *
   * @param ids A list of object IDs for the stakes to retrieve.
   * @param limit An optional integer to specify the maximum number of stakes to return per page.
   * @param cursor An optional cursor string for pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetStakesByIdsQuery.Data] object with a list of stakes and a
   *   pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getStakesByIds(
    ids: List<String>,
    limit: Int?,
    cursor: String?,
  ): Result<GetStakesByIdsQuery.Data?, SuiError> = getStakesByIds(config, ids, limit, cursor)

  /**
   * Fetches the Annual Percentage Yield (APY) for all active validators.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetValidatorsApyQuery.Data] object with a list of validators and
   *   their calculated APYs.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getValidatorApy(): Result<GetValidatorsApyQuery.Data?, SuiError> =
    getValidatorsApy(config)
}
