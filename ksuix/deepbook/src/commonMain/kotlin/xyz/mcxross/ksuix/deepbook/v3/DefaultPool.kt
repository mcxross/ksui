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
package xyz.mcxross.ksuix.deepbook.v3

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.ObjectId
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.prebuilt.Clock
import xyz.mcxross.ksuix.deepbook.v3.model.Market
import xyz.mcxross.ksuix.deepbook.v3.model.OrderType
import xyz.mcxross.ksuix.deepbook.v3.model.SelfMatchingOptions
import xyz.mcxross.ksuix.deepbook.v3.protocol.BalanceManager
import xyz.mcxross.ksuix.deepbook.v3.protocol.Pool

data class DefaultPool(override val maker: DeepBookMarketMaker, val market: Market) : Pool {
  override suspend fun refresh(): Option<ObjectArg.SharedObject> {
    return when (val pool = maker.sui.getObject(market.address)) {
      is Option.Some -> {
        Option.Some(
          ObjectArg.SharedObject(
            id =
              ObjectId(
                AccountAddress.fromString(
                  pool.value?.`object`?.objectId ?: throw Exception("No object id found")
                )
              ),
            initialSharedVersion = 110833895L,
            mutable = false,
          )
        )
      }
      Option.None -> Option.None
    }
  }

  override suspend fun whitelisted() =
      xyz.mcxross.ksuix.deepbook.v3.internal.whitelisted(maker, this)

  override suspend fun getQuoteQuantityOut(baseQuantity: Long, clock: Clock) =
      xyz.mcxross.ksuix.deepbook.v3.internal.getQuoteQuantityOut(
          maker,
          this,
          baseQuantity,
          clock,
      )

  override suspend fun getBaseQuantityOut(quoteQuantity: Long, clock: Clock) =
      xyz.mcxross.ksuix.deepbook.v3.internal.getBaseQuantityOut(
          maker,
          this,
          quoteQuantity,
          clock,
      )

  override suspend fun getQuantityOut(baseQuantity: Long, quoteQuantity: Long, clock: Clock) =
      xyz.mcxross.ksuix.deepbook.v3.internal.getQuantityOut(
          maker,
          this,
          baseQuantity,
          quoteQuantity,
          clock,
      )

  override suspend fun midPrice(clock: Clock) =
      xyz.mcxross.ksuix.deepbook.v3.internal.midPrice(maker, this, clock)

  override suspend fun accountOpenOrders(balanceManager: BalanceManager) =
      xyz.mcxross.ksuix.deepbook.v3.internal.accountOpenOrders(maker, this, balanceManager)

  override suspend fun getLevel2Range(priceLow: Long, priceHigh: Long, isBid: Boolean) =
      xyz.mcxross.ksuix.deepbook.v3.internal.getLevel2Range(
          maker,
          this,
          priceLow,
          priceHigh,
          isBid,
      )

  override suspend fun getLevel2TicksFromMid(ticks: Long, clock: Clock) =
      xyz.mcxross.ksuix.deepbook.v3.internal.getLevel2TicksFromMid(maker, this, ticks, clock)

  override suspend fun vaultBalances() =
      xyz.mcxross.ksuix.deepbook.v3.internal.vaultBalances(maker, this)

  override suspend fun placeLimitOrder(
      balanceManager: BalanceManager,
      clientOrderId: String,
      orderType: OrderType,
      selfMatchingOption: SelfMatchingOptions,
      price: Long,
      quantity: Long,
      isBid: Boolean,
      payWithDeep: Boolean,
      expireTimestamp: Long,
      clock: Clock,
  ) =
      xyz.mcxross.ksuix.deepbook.v3.internal.placeLimitOrder(
          maker,
          this,
          balanceManager,
          clientOrderId,
          orderType,
          selfMatchingOption,
          price,
          quantity,
          isBid,
          payWithDeep,
          expireTimestamp,
          clock,
      )
}
