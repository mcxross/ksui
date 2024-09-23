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

import xyz.mcxross.ksui.generated.ExecuteTransactionBlock
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksuix.deepbook.v3.internal.balance
import xyz.mcxross.ksuix.deepbook.v3.internal.deposit
import xyz.mcxross.ksuix.deepbook.v3.internal.generateProofAsOwner
import xyz.mcxross.ksuix.deepbook.v3.internal.mintTradeCap
import xyz.mcxross.ksuix.deepbook.v3.internal.withdraw
import xyz.mcxross.ksuix.deepbook.v3.internal.withdrawAll
import xyz.mcxross.ksuix.deepbook.v3.model.TradeCap
import xyz.mcxross.ksuix.deepbook.v3.model.TradeProof
import xyz.mcxross.ksuix.deepbook.v3.protocol.BalanceManager

class DefaultBalanceManager(override val maker: DeepBookMarketMaker, val address: AccountAddress) :
    BalanceManager {

  override suspend fun refresh(): Option<ObjectArg.ImmOrOwnedObject> {
    return when (val obj = maker.sui.getObject(id())) {
      is Option.Some -> {
        Option.Some(
          ObjectArg.ImmOrOwnedObject.from(
            address,
            obj.value?.`object`?.version ?: throw Exception("No object version found"),
            obj.value!!.`object`!!.digest ?: throw Exception("No object digest found"),
          )
        )
      }
      is Option.None -> throw Exception("No object found")
    }
  }

  override suspend fun new(receipt: AccountAddress?): Option.Some<ExecuteTransactionBlock.Result?> =
      xyz.mcxross.ksuix.deepbook.v3.internal.new(maker = maker, receipt = receipt)

  override suspend fun mintTradeCap(
    receipt: AccountAddress?
  ): Option.Some<ExecuteTransactionBlock.Result?> =
    mintTradeCap(maker = maker, balanceManager = this, receipt = receipt)

  override suspend fun revokeTradeCap(
    receipt: AccountAddress?
  ): Option.Some<ExecuteTransactionBlock.Result?> {
    TODO("Not yet implemented")
  }

  override suspend fun generateProofAsOwner(): Option.Some<ExecuteTransactionBlock.Result?> =
    generateProofAsOwner(maker = maker, balanceManager = this)

  override suspend fun generateProofAsTrader(
    tradeCap: TradeCap
  ): Option.Some<ExecuteTransactionBlock.Result?> =
      xyz.mcxross.ksuix.deepbook.v3.internal.generateProofAsTrader(
          maker = maker,
          balanceManager = this,
          tradeCap = tradeCap,
      )

  override suspend fun deposit(
    amount: ULong,
    type: String,
  ): Option<ExecuteTransactionBlock.Result?> =
    deposit(maker = maker, balanceManager = this, amount = amount, type = type)

  override suspend fun withdraw(
    amount: ULong,
    type: String,
    receipt: AccountAddress,
  ): Option.Some<ExecuteTransactionBlock.Result?> =
    withdraw(maker = maker, balanceManager = this, amount = amount, type = type, receipt = receipt)

  override suspend fun withdrawAll(
    type: String,
    receipt: AccountAddress,
  ): Option.Some<ExecuteTransactionBlock.Result?> =
    withdrawAll(maker = maker, balanceManager = this, type = type, receipt = receipt)

  override suspend fun validateProof(
    proof: TradeProof
  ): Option.Some<ExecuteTransactionBlock.Result?> =
      xyz.mcxross.ksuix.deepbook.v3.internal.validateProof(
          maker = maker,
          balanceManager = this,
      )

  override suspend fun balance(type: String): Option.Some<ExecuteTransactionBlock.Result?> =
    balance(maker = maker, balanceManager = this, type = type)

  override suspend fun owner(): Option.Some<ExecuteTransactionBlock.Result?> {
    TODO("Not yet implemented")
  }

  override fun id(): String = address.toString()
}
