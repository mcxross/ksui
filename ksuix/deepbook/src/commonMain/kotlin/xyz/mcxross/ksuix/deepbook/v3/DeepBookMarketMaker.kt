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

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.generated.ExecuteTransactionBlock
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksuix.deepbook.DeepBook
import xyz.mcxross.ksuix.deepbook.v3.model.TradeCap
import xyz.mcxross.ksuix.deepbook.v3.protocol.BalanceManager

class DeepBookMarketMaker(val sui: Sui = Sui(), val owner: Account) : DeepBook {

  var balanceManagers: MutableList<BalanceManager> = mutableListOf()
  var tradeCaps: MutableList<TradeCap> = mutableListOf()

  private var workingBalanceManager: Option<BalanceManager> = Option.None
  var workingTradeCap: Option<TradeCap> = Option.None

  suspend fun populate() {

    val retrievedBalanceManagers =
      sui.getOwnedObjectsByType(
        owner = owner.address,
        "0x2c152dba0110d3afb76b659ed3436edd848b37e177c3abfc0296f8aefc2e6cf4::balance_manager::BalanceManager",
      )

    if (retrievedBalanceManagers is Option.Some) {
      retrievedBalanceManagers.value.forEach {
        balanceManagers.add(DefaultBalanceManager(this, AccountAddress.fromString(it.objectId)))
      }
    }

    val retrievedTradeCaps =
      sui.getOwnedObjectsByType(
        owner = owner.address,
        "0x2c152dba0110d3afb76b659ed3436edd848b37e177c3abfc0296f8aefc2e6cf4::balance_manager::TradeCap",
        option = ObjectDataOptions(showDisplay = true),
      )
    if (retrievedTradeCaps is Option.Some) {
      retrievedTradeCaps.value.forEach {
        tradeCaps.add(
          TradeCap(
            id = AccountAddress.fromString(it.objectId),
            balanceManagerId = AccountAddress.EMPTY,
          )
        )
      }
    }
  }

  fun setWorkingBalanceManager(id: String? = null): Boolean {
    workingBalanceManager = Option.Some(id?.let { getBalanceManager(it) } ?: firstBalanceManager())
    return workingBalanceManager is Option.Some
  }

  fun setWorkingTradeCap(id: String? = null) {
    workingTradeCap =
      Option.Some(id?.let { tradeCaps.find { it.id.toString() == id } } ?: firstTradeCap())
  }

  /**
   * Creates a new balance manager.
   *
   * This creates a new balance manager either for the owner of the DeepBook instance or for the
   * provided [receipt].
   *
   * @param receipt The receipt of the account for which the balance manager is to be created. This
   *   is optional and defaults to null.
   * @return A new [BalanceManager] instance.
   */
  suspend fun createBalanceManager(
    receipt: AccountAddress? = null
  ): ExecuteTransactionBlock.Result? {
    return when (workingBalanceManager) {
      is Option.Some -> {
        (workingBalanceManager as Option.Some<BalanceManager>)
          .value
          .new(receipt)
          .expect("Could not create balance manager")
      }
      is Option.None -> throw Exception("No working balance manager found")
    }
  }

  fun getWorkingBalanceManager(): BalanceManager {
    if (workingBalanceManager is Option.None) {
      throw Exception(
        "No working balance manager found. Did you forget to call setWorkingBalanceManager?"
      )
    }
    return (workingBalanceManager as Option.Some<BalanceManager>).value
  }

  fun getBalanceManager(id: String): BalanceManager {
    return balanceManagers.find { it.id() == id } ?: throw Exception("Balance manager not found")
  }

  fun firstBalanceManager(): BalanceManager {
    return balanceManagers.first()
  }

  fun firstTradeCap(): TradeCap {
    return tradeCaps.first()
  }
}
