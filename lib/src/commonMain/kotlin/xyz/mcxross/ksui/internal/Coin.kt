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
import xyz.mcxross.ksui.generated.GetAllBalances
import xyz.mcxross.ksui.generated.GetBalance
import xyz.mcxross.ksui.generated.GetCoinMetadata
import xyz.mcxross.ksui.generated.GetCoins
import xyz.mcxross.ksui.generated.GetTotalSupply
import xyz.mcxross.ksui.generated.getcoins.Coin
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Balance
import xyz.mcxross.ksui.model.Balances
import xyz.mcxross.ksui.model.CoinMetadata
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getAllBalances(config: SuiConfig, address: AccountAddress): Option<Balances?> {

  val client = getGraphqlClient(config)
  val request by lazy { GetAllBalances(GetAllBalances.Variables(address.toString())) }
  val response = client.execute(request)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal suspend fun getCoins(
  config: SuiConfig,
  address: AccountAddress,
  first: Int? = null,
  cursor: String? = null,
  type: String = "0x2::sui::SUI",
): Option<List<Coin>> {

  var currentCursor: String? = cursor
  val client = getGraphqlClient(config)
  val coins = mutableListOf<Coin>()
  var totalCoinsRetrieved = 0

  do {
    val request by lazy {
      GetCoins(
        GetCoins.Variables(
          address.toString(),
          first?.let { first - totalCoinsRetrieved },
          currentCursor,
          type,
        )
      )
    }
    val response = client.execute(request)

    if (!response.errors.isNullOrEmpty()) {
      throw SuiException(response.errors.toString())
    }

    val data = response.data ?: return Option.None
    val coinList = data.address?.coins?.nodes ?: emptyList()

    coins.addAll(coinList)
    totalCoinsRetrieved += coinList.size

    currentCursor =
      if (data.address?.coins?.pageInfo?.hasNextPage == true) {
        data.address.coins.pageInfo.endCursor
      } else {
        null
      }
  } while (currentCursor != null && (first == null || totalCoinsRetrieved < first))

  return if (coins.isNotEmpty()) Option.Some(coins) else Option.None
}

internal suspend fun getTotalSupply(config: SuiConfig, type: String): Option<String> {
  val client = getGraphqlClient(config)
  val request by lazy { GetTotalSupply(GetTotalSupply.Variables(type)) }
  val response = client.execute(request)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!.coinMetadata?.supply.toString())
}

internal suspend fun getBalance(
  config: SuiConfig,
  address: AccountAddress,
  type: String?,
): Option<Balance> {
  val client = getGraphqlClient(config)
  val request by lazy { GetBalance(GetBalance.Variables(address.toString(), type)) }
  val response = client.execute(request)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal suspend fun getCoinMetadata(config: SuiConfig, type: String): Option<CoinMetadata> {
  val client = getGraphqlClient(config)
  val request by lazy { GetCoinMetadata(GetCoinMetadata.Variables(type)) }
  val response = client.execute(request)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}
