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
import xyz.mcxross.ksui.generated.GetCommitteeInfo
import xyz.mcxross.ksui.generated.GetStakes
import xyz.mcxross.ksui.generated.GetStakesByIds
import xyz.mcxross.ksui.generated.GetValidatorsApy
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.CommitteeInfo
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Stake
import xyz.mcxross.ksui.model.Stakes
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.ValidatorsApy

internal suspend fun getCommitteeInfo(
  config: SuiConfig,
  epochId: Long?,
  after: String?,
): Option<CommitteeInfo> {
  val response =
    getGraphqlClient(config)
      .execute(
        GetCommitteeInfo(GetCommitteeInfo.Variables(epochId = epochId?.toInt(), after = after))
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal suspend fun getStakes(
  config: SuiConfig,
  address: AccountAddress,
  limit: Int?,
  cursor: String?,
): Option<Stake> {
  val response =
    getGraphqlClient(config)
      .execute(
        GetStakes(GetStakes.Variables(owner = address.toString(), limit = limit, cursor = cursor))
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal suspend fun getStakesById(
  config: SuiConfig,
  ids: List<String>,
  limit: Int?,
  cursor: String?,
): Option<Stakes> {
  val query = GetStakesByIds(GetStakesByIds.Variables(ids = ids, limit = limit, cursor = cursor))

  val response = getGraphqlClient(config).execute(query)

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal suspend fun getValidatorsApy(config: SuiConfig): Option<ValidatorsApy> {
  val response = getGraphqlClient(config).execute(GetValidatorsApy())

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}
