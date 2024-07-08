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

import xyz.mcxross.ksui.model.CommitteeInfo
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Stake
import xyz.mcxross.ksui.model.Stakes
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.ValidatorsApy

/**
 * Governance interface
 *
 * This interface represents the governance API
 */
interface Governance {

  /**
   * Get the committee info
   *
   * @param epochId The epoch ID to get the committee info for
   * @param after The cursor to get the committee info after
   * @return An [Option] of nullable [CommitteeInfo]
   */
  suspend fun getCommitteeInfo(epochId: Long? = null, after: String? = null): Option<CommitteeInfo>

  /**
   * Get the stakes
   *
   * @param owner The owner to get the stakes for
   * @param limit The limit of stakes to get
   * @param cursor The cursor to get the stakes from
   * @return An [Option] of nullable [Stake]
   */
  suspend fun getStakes(
    owner: SuiAddress,
    limit: Int? = null,
    cursor: String? = null,
  ): Option<Stake>

  /**
   * Get the stakes by IDs
   *
   * @param ids The IDs to get the stakes for
   * @param limit The limit of stakes to get
   * @param cursor The cursor to get the stakes from
   * @return An [Option] of nullable [Stakes]
   */
  suspend fun getStakesByIds(
    ids: List<String>,
    limit: Int? = null,
    cursor: String? = null,
  ): Option<Stakes>

  /**
   * Get the validator APY
   *
   * @return An [Option] of nullable [ValidatorsApy]
   */
  suspend fun getValidatorApy(): Option<ValidatorsApy>
}
