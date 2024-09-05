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

import xyz.mcxross.ksui.internal.getAllBalances
import xyz.mcxross.ksui.internal.getBalance
import xyz.mcxross.ksui.internal.getCoinMetadata
import xyz.mcxross.ksui.internal.getCoins
import xyz.mcxross.ksui.internal.getTotalSupply
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Balance
import xyz.mcxross.ksui.model.Balances
import xyz.mcxross.ksui.model.CoinMetadata
import xyz.mcxross.ksui.model.Coins
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Coin

/**
 * Coin implementation
 *
 * This class represents the coin API
 */
class Coin(val config: SuiConfig) : Coin {

  /**
   * Get all balances for an address
   *
   * @param address The address to get balances for
   * @return An [Option] of nullable [Balances]
   */
  override suspend fun getAllBalances(address: AccountAddress): Option<Balances> =
    getAllBalances(config, address)

  /**
   * Get coins for an address
   *
   * @param address The address to get coins for
   * @param first The number of coins to get
   * @param cursor The cursor to get coins from
   * @param type The type of coins to get
   * @return An [Option] of nullable [Coins]
   */
  override suspend fun getCoins(
    address: AccountAddress,
    first: Int?,
    cursor: String?,
    type: String,
  ): Option<List<xyz.mcxross.ksui.generated.getcoins.Coin>> =
    getCoins(config, address, first, cursor, type)

  /**
   * Get the total supply of a coin
   *
   * @param type The type of coin to get the total supply for
   * @return An [Option] of nullable [String]
   */
  override suspend fun getTotalSupply(type: String): Option<String> = getTotalSupply(config, type)

  /**
   * Get the balance of an address for a specific coin type
   *
   * If the type is not provided, the default coin type of `0x2::sui::SUI` will be used
   *
   * @param address The address to get the balance for
   * @return An [Option] of nullable [Balance]
   */
  override suspend fun getBalance(address: AccountAddress, type: String?): Option<Balance> =
    getBalance(config, address, type)

  /**
   * Get the metadata for a coin
   *
   * @param type The type of coin to get the metadata for
   * @return An [Option] of nullable [CoinMetadata]
   */
  override suspend fun getCoinMetadata(type: String): Option<CoinMetadata> =
    getCoinMetadata(config, type)
}
