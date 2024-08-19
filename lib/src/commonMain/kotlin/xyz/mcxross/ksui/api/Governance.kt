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

import xyz.mcxross.ksui.internal.getCommitteeInfo
import xyz.mcxross.ksui.internal.getStakes
import xyz.mcxross.ksui.internal.getStakesById
import xyz.mcxross.ksui.internal.getValidatorsApy
import xyz.mcxross.ksui.model.CommitteeInfo
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Stake
import xyz.mcxross.ksui.model.Stakes
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.ValidatorsApy
import xyz.mcxross.ksui.protocol.Governance

/**
 * Governance API implementation
 *
 * This namespace contains all the functions related to governance
 *
 * @property config The SuiConfig to use
 */
class Governance(val config: SuiConfig) : Governance {

  /**
   * Get the committee info
   *
   * @param epochId The epoch ID to get the committee info for
   * @param after The cursor to get the committee info from
   * @return An [Option] of nullable [CommitteeInfo]
   */
  override suspend fun getCommitteeInfo(epochId: Long?, after: String?): Option<CommitteeInfo> =
    getCommitteeInfo(config, epochId, after)

  /**
   * Get stakes for an address
   *
   * @param owner The address to get stakes for
   * @param limit The limit of stakes to get
   * @param cursor The cursor to get stakes from
   * @return An [Option] of nullable [Stake]
   */
  override suspend fun getStakes(owner: SuiAddress, limit: Int?, cursor: String?): Option<Stake> =
    getStakes(config, owner, limit, cursor)

  /**
   * Get stakes by IDs
   *
   * @param ids The IDs of the stakes to get
   * @param limit The limit of stakes to get
   * @param cursor The cursor to get stakes from
   * @return An [Option] of nullable [Stakes]
   */
  override suspend fun getStakesByIds(
    ids: List<String>,
    limit: Int?,
    cursor: String?,
  ): Option<Stakes> = getStakesById(config, ids, limit, cursor)

  /**
   * Get the validators APY
   *
   * @return An [Option] of nullable [ValidatorsApy]
   */
  override suspend fun getValidatorApy(): Option<ValidatorsApy> = getValidatorsApy(config)
}
