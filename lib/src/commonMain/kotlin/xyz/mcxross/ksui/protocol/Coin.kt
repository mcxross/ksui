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

import xyz.mcxross.ksui.generated.getaddressbalance.Address
import xyz.mcxross.ksui.generated.getcoinmetadata.CoinMetadata
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiAddress

interface Coin {
  suspend fun getAllBalances(address: SuiAddress): Option<Address?>

  suspend fun getAllCoins(address: SuiAddress, type: String, limit: Int): Option<xyz.mcxross.ksui.generated.getcoinsbytypeandowner.Address?>

  suspend fun getCoins(
    address: SuiAddress,
    cursor: String? = null,
    limit: Int? = null,
  ): Option<xyz.mcxross.ksui.generated.getcoinsbyowner.Address?>

  suspend fun getSupply(
    type: String
  ): Option<xyz.mcxross.ksui.generated.getcoinsupply.CoinMetadata?>

  suspend fun getBalance(address: SuiAddress): Option<Address?>

  suspend fun getCoinMetadata(type: String): Option<CoinMetadata?>
}
