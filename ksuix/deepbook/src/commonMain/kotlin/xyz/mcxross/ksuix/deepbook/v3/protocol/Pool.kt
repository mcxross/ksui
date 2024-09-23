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
package xyz.mcxross.ksuix.deepbook.v3.protocol

import xyz.mcxross.ksui.model.ExecuteTransactionBlockResult
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.prebuilt.Clock
import xyz.mcxross.ksuix.deepbook.v3.DeepBookMarketMaker
import xyz.mcxross.ksuix.deepbook.v3.model.OrderType
import xyz.mcxross.ksuix.deepbook.v3.model.SelfMatchingOptions

/**
 * A pool is a collection of liquidity that can be used to trade assets.
 *
 * This is implemented as a shared object representing a market. It used for managing the market's
 * order book, users, stakes, and so on.
 */
interface Pool {

  val maker: DeepBookMarketMaker

  suspend fun refresh(): Option<ObjectArg.SharedObject>

  suspend fun whitelisted(): Option.Some<ExecuteTransactionBlockResult>

  suspend fun getQuoteQuantityOut(
    baseQuantity: Long,
    clock: Clock = Clock(maker.sui),
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun getBaseQuantityOut(
    quoteQuantity: Long,
    clock: Clock = Clock(maker.sui),
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun getQuantityOut(
    baseQuantity: Long,
    quoteQuantity: Long,
    clock: Clock = Clock(maker.sui),
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun midPrice(
    clock: Clock = Clock(maker.sui)
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun accountOpenOrders(
    balanceManager: BalanceManager
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun getLevel2Range(
    priceLow: Long,
    priceHigh: Long,
    isBid: Boolean,
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun getLevel2TicksFromMid(
    ticks: Long,
    clock: Clock = Clock(maker.sui),
  ): Option.Some<ExecuteTransactionBlockResult>

  suspend fun vaultBalances(): Option.Some<ExecuteTransactionBlockResult>

  suspend fun placeLimitOrder(
      balanceManager: BalanceManager,
      clientOrderId: String,
      orderType: OrderType,
      selfMatchingOption: SelfMatchingOptions,
      price: Long,
      quantity: Long,
      isBid: Boolean,
      payWithDeep: Boolean,
      expireTimestamp: Long,
      clock: Clock = Clock(maker.sui),
  ): Option.Some<ExecuteTransactionBlockResult>
}
