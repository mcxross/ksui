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
package xyz.mcxross.ksuix.deepbook.v3.internal

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.generated.ExecuteTransactionBlock
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Object
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Struct
import xyz.mcxross.ksui.prebuilt.Clock
import xyz.mcxross.ksui.ptb.programmableTx
import xyz.mcxross.ksui.util.inputs
import xyz.mcxross.ksui.util.types
import xyz.mcxross.ksuix.deepbook.v3.DeepBookMarketMaker
import xyz.mcxross.ksuix.deepbook.v3.DefaultPool
import xyz.mcxross.ksuix.deepbook.v3.extension.contractAddress
import xyz.mcxross.ksuix.deepbook.v3.model.OrderType
import xyz.mcxross.ksuix.deepbook.v3.model.SelfMatchingOptions
import xyz.mcxross.ksuix.deepbook.v3.protocol.BalanceManager

private suspend fun performTxn(
  maker: DeepBookMarketMaker,
  target: String,
  pool: DefaultPool,
  sharedPoolObj: ObjectArg.SharedObject,
): Option.Some<ExecuteTransactionBlock.Result?> {
  val ptb = programmableTx {
    command {
      moveCall {
        this.target = target

        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = inputs(sharedPoolObj)
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun whitelisted(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
): Option.Some<ExecuteTransactionBlock.Result?> {
  return when (val refreshedPool = pool.refresh()) {
    is Option.Some -> {
      performTxn(
        maker,
        target = "${maker.contractAddress()}::pool::whitelisted",
        pool,
        refreshedPool.value,
      )
    }
    Option.None -> {
      throw Exception("Pool not found")
    }
  }
}

internal suspend fun getQuoteQuantityOut(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  baseQuantity: Long,
  clock: Clock,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::get_quote_quantity_out"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = listOf(input(poolFresh), input(baseQuantity), input(clocked))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun getBaseQuantityOut(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  quoteQuantity: Long,
  clock: Clock,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::get_base_quantity_out"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = listOf(input(poolFresh), input(quoteQuantity), input(clocked))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun getQuantityOut(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  baseQuantity: Long,
  quoteQuantity: Long,
  clock: Clock,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::get_quantity_out"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments =
          listOf(input(poolFresh), input(baseQuantity), input(quoteQuantity), input(clocked))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun midPrice(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  clock: Clock,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::mid_price"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = listOf(input(poolFresh), input(clocked))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun accountOpenOrders(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  balanceManager: BalanceManager,
): Option.Some<ExecuteTransactionBlock.Result?> {
  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::account_open_orders"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = listOf(input(poolFresh), input(balanceManager.id()))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun getLevel2Range(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  priceLow: Long,
  priceHigh: Long,
  isBid: Boolean,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val clock = Clock(maker.sui)

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::get_level2_range"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments =
          listOf(input(poolFresh), input(priceLow), input(priceHigh), input(isBid), input(clocked))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun getLevel2TicksFromMid(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
  ticks: Long,
  clock: Clock,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::get_level2_ticks_from_mid"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = listOf(input(poolFresh), input(ticks), input(clocked))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

internal suspend fun vaultBalances(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
): Option.Some<ExecuteTransactionBlock.Result?> {
  val poolFresh: ObjectArg.SharedObject =
    when (val refreshedPool = pool.refresh()) {
      is Option.Some -> {
        refreshedPool.value
      }
      Option.None -> {
        throw Exception("Pool not found")
      }
    }

  val ptb = programmableTx {
    command {
      moveCall {
        this.target = "${maker.contractAddress()}::pool::vault_balances"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments = listOf(input(poolFresh))
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}

private suspend fun getBalanceManagerCurrentInfo(
  sui: Sui,
  balanceManager: BalanceManager,
): Option<Object> {
  return sui.getObject(balanceManager.id())
}

private suspend fun liveObjectData(sui: Sui, balanceManager: BalanceManager) =
  when (val obj = getBalanceManagerCurrentInfo(sui, balanceManager)) {
    is Option.Some -> {
      Pair(
        obj.value?.`object`?.version ?: throw Exception("No object version found"),
        obj.value!!.`object`?.digest ?: throw Exception("No object digest found"),
      )
    }
    is Option.None -> throw Exception("No object found")
  }

internal suspend fun placeLimitOrder(
  maker: DeepBookMarketMaker,
  pool: DefaultPool,
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
): Option.Some<ExecuteTransactionBlock.Result?> {

  val liveData = liveObjectData(maker.sui, balanceManager)

  val clocked =
    when (val refreshedClock = clock.refresh()) {
      is Option.Some -> {
        ObjectArg.SharedObject(
          id = refreshedClock.value.id,
          initialSharedVersion = refreshedClock.value.initialSharedVersion,
          mutable = refreshedClock.value.mutable,
        )
      }
      Option.None -> {
        throw Exception("Clock not found")
      }
    }

  val ptb = programmableTx {
    command {
      val two =
        inputs(
          ObjectArg.ImmOrOwnedObject.from(
            AccountAddress.fromString(balanceManager.id()),
            version = liveData.first,
            digest = liveData.second,
          )
        )

      val tradeProof = moveCall {
        this.target = "${maker.contractAddress()}::balance_manager::generate_proof_as_owner"
        arguments = two
      }

      moveCall {
        this.target = "${maker.contractAddress()}::pool::place_limit_order"
        typeArguments =
          types(Struct.from(pool.market.baseAsset.type), Struct.from(pool.market.quoteAsset.type))

        arguments =
          listOf(
            input(0UL),
            input(two),
            input(tradeProof),
            input(clientOrderId),
            input(OrderType.NO_RESTRICTION),
            input(SelfMatchingOptions.SELF_MATCHING_ALLOWED),
            input(1UL),
            input(10UL),
            input(true),
            input(true),
            input(expireTimestamp),
            input(clocked),
          )
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  return response
}
