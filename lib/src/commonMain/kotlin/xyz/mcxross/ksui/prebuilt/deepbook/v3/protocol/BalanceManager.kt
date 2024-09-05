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
package xyz.mcxross.ksui.prebuilt.deepbook.v3.protocol

import xyz.mcxross.ksui.generated.ExecuteTransactionBlock
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.prebuilt.deepbook.v3.DeepBookMarketMaker
import xyz.mcxross.ksui.prebuilt.deepbook.v3.model.TradeCap
import xyz.mcxross.ksui.prebuilt.deepbook.v3.model.TradeProof

interface BalanceManager {

  val maker: DeepBookMarketMaker

  /**
   * Refreshes the balance manager object.
   *
   * This is typically required when making transactions with the balance manager, as versions and
   * digests are "living" and can change.
   */
  suspend fun refresh(): Option<ObjectArg.ImmOrOwnedObject>

  /**
   * Creates a new balance manager object.
   *
   * Optionally, you can specify a receipt address to send the object to. Defaults to the owner's
   * address. This is the account that created the [DeepBookMarketMaker].
   *
   * @param receipt an optional receipt address to send the object to
   */
  suspend fun new(receipt: AccountAddress? = null): Option.Some<ExecuteTransactionBlock.Result?>

  /**
   * Mints a trade cap for the balance manager.
   *
   * Optionally, you can specify a receipt address to send the object to. Defaults to the owner's
   * address. This is the account that created the [DeepBookMarketMaker]. This is typically used by
   * the bearer to place orders with the [BalanceManager]. The bearer can not, however, deposit or
   * withdraw from the [BalanceManager]. The maximum number of TradeCap that can be assigned for a
   * BalanceManager is `1000`
   *
   * @param receipt an optional receipt address to send the object to
   */
  suspend fun mintTradeCap(
    receipt: AccountAddress? = null
  ): Option.Some<ExecuteTransactionBlock.Result?>

  suspend fun revokeTradeCap(
    receipt: AccountAddress? = null
  ): Option.Some<ExecuteTransactionBlock.Result?>

  suspend fun generateProofAsOwner(): Option.Some<ExecuteTransactionBlock.Result?>

  suspend fun generateProofAsTrader(
    tradeCap: TradeCap
  ): Option.Some<ExecuteTransactionBlock.Result?>

  /**
   * Deposits an amount of the specified type into this [BalanceManager] by the owner.
   *
   * Optionally, you can specify the type of the deposit. Defaults to `0x02::sui::SUI`.
   *
   * @param amount the amount to deposit
   * @param type the type of the deposit
   */
  suspend fun deposit(
    amount: ULong,
    type: String = "0x02::sui::SUI",
  ): Option<ExecuteTransactionBlock.Result?>

  /**
   * Withdraws an amount of the specified type from this [BalanceManager] by the owner.
   *
   * Optionally, you can specify the type of the withdrawal. Defaults to `0x02::sui::SUI`.
   *
   * @param amount the amount to withdraw
   * @param type the type of the withdrawal
   */
  suspend fun withdraw(
    amount: ULong,
    type: String = "0x02::sui::SUI",
    receipt: AccountAddress = maker.owner.address,
  ): Option.Some<ExecuteTransactionBlock.Result?>

  /**
   * Withdraws all the specified type balance from this [BalanceManager] by the owner.
   *
   * Optionally, you can specify the type of the withdrawal and the receipt address. Defaults to
   * `0x02::sui::SUI` and `null`, respectively. In the case of the receipt address, the balance will
   * be sent to the owner's address.
   *
   * @param type the type of the withdrawal
   */
  suspend fun withdrawAll(
    type: String = "0x02::sui::SUI",
    receipt: AccountAddress = maker.owner.address,
  ): Option.Some<ExecuteTransactionBlock.Result?>

  /**
   * Validate that this [TradeProof] can access this balance manager's funds.
   *
   * @param proof the trade proof to validate
   */
  suspend fun validateProof(proof: TradeProof): Option.Some<ExecuteTransactionBlock.Result?>

  /**
   * Gets the balance of the specified type in this [BalanceManager].
   *
   * Optionally, you can specify the type of the balance. Defaults to `0x02::sui::SUI`.
   *
   * @param type the type of the balance
   */
  suspend fun balance(type: String = "0x02::sui::SUI"): Option.Some<ExecuteTransactionBlock.Result?>

  /** Gets the owner of this [BalanceManager]. */
  suspend fun owner(): Option.Some<ExecuteTransactionBlock.Result?>

  /** Gets the ID of this [BalanceManager]. */
  fun id(): String
}
