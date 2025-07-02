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
package xyz.mcxross.ksui.protocol

import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.GetCommitteeInfoQuery
import xyz.mcxross.ksui.generated.GetStakesByIdsQuery
import xyz.mcxross.ksui.generated.GetStakesQuery
import xyz.mcxross.ksui.generated.GetValidatorsApyQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

/**
 * Defines the API for interacting with Sui's on-chain governance, including validators, committees,
 * and staking.
 */
interface Governance {

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
  suspend fun getCommitteeInfo(
    epochId: Long? = null,
    after: String? = null,
  ): Result<GetCommitteeInfoQuery.Data?, SuiError>

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
  suspend fun getStakes(
    owner: AccountAddress,
    limit: Int? = null,
    cursor: String? = null,
  ): Result<GetStakesQuery.Data?, SuiError>

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
  suspend fun getStakesByIds(
    ids: List<String>,
    limit: Int? = null,
    cursor: String? = null,
  ): Result<GetStakesByIdsQuery.Data?, SuiError>

  /**
   * Fetches the Annual Percentage Yield (APY) for all active validators.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetValidatorsApyQuery.Data] object with a list of validators and
   *   their calculated APYs.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getValidatorApy(): Result<GetValidatorsApyQuery.Data?, SuiError>
}
