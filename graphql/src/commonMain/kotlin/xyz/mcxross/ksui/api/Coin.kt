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
import xyz.mcxross.ksui.generated.GetAllBalancesQuery
import xyz.mcxross.ksui.generated.GetBalanceQuery
import xyz.mcxross.ksui.generated.GetCoinMetadataQuery
import xyz.mcxross.ksui.generated.GetCoinsQuery
import xyz.mcxross.ksui.generated.GetTotalSupplyQuery
import xyz.mcxross.ksui.internal.getAllBalances
import xyz.mcxross.ksui.internal.getBalance
import xyz.mcxross.ksui.internal.getCoinMetadata
import xyz.mcxross.ksui.internal.getCoins
import xyz.mcxross.ksui.internal.getTotalSupply
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Coin

/**
 * Coin implementation
 *
 * This class represents the coin API
 */
class Coin(val config: SuiConfig) : Coin {
  /**
   * Fetches the balance of a specific coin type for a given address.
   *
   * If the `coinType` parameter is not provided, the function defaults to fetching the balance for
   * the native SUI coin (`0x2::sui::SUI`).
   *
   * @param address The [AccountAddress] for which to get the balance.
   * @param coinType An optional string representing the coin type (e.g., "0x2::sui::SUI"). Defaults
   *   to `0x2::sui::SUI` if `null`.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetBalanceQuery.Data] object with the balance details. The data
   *   can be `null` if the address holds no coins of the specified type.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getBalance(
    address: AccountAddress,
    coinType: String?,
  ): Result<GetBalanceQuery.Data?, SuiError> = getBalance(config, address, coinType)

  /**
   * Fetches all coin balances for a given Sui address.
   *
   * This method supports pagination to allow for fetching large sets of balances incrementally.
   *
   * @param address The [AccountAddress] of the account to query.
   * @param limit An optional integer to specify the maximum number of balances to return per page.
   * @param cursor An optional cursor string for pagination. Use the `nextCursor` from a previous
   *   response to fetch the next page of results.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetAllBalancesQuery.Data] object. This object includes a list of
   *   balances and a `nextCursor` field for pagination.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getAllBalances(
    address: AccountAddress,
    limit: Int?,
    cursor: String?,
  ): Result<GetAllBalancesQuery.Data?, SuiError> = getAllBalances(config, address, limit, cursor)

  /**
   * Fetches coin objects owned by a given address.
   *
   * This method supports pagination and can be filtered by a specific coin type.
   *
   * @param address The [AccountAddress] of the owner.
   * @param first An optional integer to specify the maximum number of coin objects to return per
   *   page.
   * @param cursor An optional cursor string for pagination. Use the `nextCursor` from a previous
   *   response to fetch the next page of results.
   * @param type An optional string representing the coin type to filter by (e.g., "0x2::sui::SUI").
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetCoinsQuery.Data] object. This object includes a list of coin
   *   objects and a `nextCursor` field for pagination.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getCoins(
    address: AccountAddress,
    first: Int?,
    cursor: String?,
    type: String?,
  ): Result<GetCoinsQuery.Data?, SuiError> = getCoins(config, address, first, cursor, type)

  /**
   * Fetches the total supply for a given coin type.
   *
   * @param type The string representing the coin type (e.g., "0x2::sui::SUI").
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetTotalSupplyQuery.Data] object with the total supply details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getTotalSupply(type: String): Result<GetTotalSupplyQuery.Data?, SuiError> =
    getTotalSupply(config, type)

  /**
   * Fetches the metadata for a specific coin type.
   *
   * Metadata may include the coin's name, symbol, description, and number of decimals.
   *
   * @param type The string representing the coin type for which to fetch metadata.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetCoinMetadataQuery.Data] object with the coin's metadata.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getCoinMetadata(type: String): Result<GetCoinMetadataQuery.Data?, SuiError> =
    getCoinMetadata(config, type)
}
