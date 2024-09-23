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

import xyz.mcxross.ksui.generated.enums.ExecutionStatus
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResult
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Struct
import xyz.mcxross.ksuix.deepbook.v3.DeepBookMarketMaker
import xyz.mcxross.ksuix.deepbook.v3.extension.contractAddress
import xyz.mcxross.ksuix.deepbook.v3.model.TradeCap
import xyz.mcxross.ksuix.deepbook.v3.protocol.BalanceManager
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.programmableTx
import xyz.mcxross.ksui.util.inputs
import xyz.mcxross.ksui.util.types

internal suspend fun new(
    maker: DeepBookMarketMaker,
    receipt: AccountAddress?,
): Option.Some<ExecuteTransactionBlockResult> {
  val ptb = programmableTx {
    command {
      val balanceManager = moveCall { target = "${maker.contractAddress()}::balance_manager::new" }
      transferObjects {
        objects = inputs(balanceManager)
        to = input(receipt ?: maker.owner.address)
      }
    }
  }
  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun mintTradeCap(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
    receipt: AccountAddress?,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    command {
      val tradeCap = moveCall {
        target = "${maker.contractAddress()}::balance_manager::mint_trade_cap"
        typeArguments = emptyList()
        arguments = inputs(refreshedBalanceManager)
      }
      transferObjects {
        objects = inputs(tradeCap)
        to = input(receipt ?: maker.owner.address)
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun generateProofAsOwner(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    command {
      moveCall {
        target = "${maker.contractAddress()}::balance_manager::generate_proof_as_owner"
        arguments = inputs(refreshedBalanceManager)
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun generateProofAsTrader(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
    tradeCap: TradeCap,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    command {
      moveCall {
        target = "${maker.contractAddress()}::balance_manager::generate_proof_as_trader"
        arguments =
          inputs(
            refreshedBalanceManager,
            ObjectArg.ImmOrOwnedObject.from(AccountAddress.EMPTY, 0, ""),
          )
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun deposit(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
    amount: ULong,
    type: String,
): Option<ExecuteTransactionBlockResult> {

  val ptb = programmableTx {
    command {
      val splitCoins = splitCoins {
        coin = Argument.GasCoin
        into = inputs(amount)
      }
      transferObjects {
        objects = inputs(splitCoins)
        to = input(maker.owner.address)
      }
    }
  }

  val response = maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)

  if (response.value != null) {
    if (response.value!!.executeTransactionBlock.effects.status == ExecutionStatus.SUCCESS) {
      when (val coins = maker.sui.getCoins(maker.owner.address, type = type)) {
        is Option.Some -> {

          val refreshedBalanceManager =
            balanceManager.refresh().expect("Could not refresh balance manager")

          val ourCoin = coins.value.filter { it.coinBalance == amount.toString() }[0]

          val balanceManagerDepositPTB = programmableTx {
            command {
              moveCall {
                target = "${maker.contractAddress()}::balance_manager::deposit"
                typeArguments = types(Struct.from(type))
                arguments =
                  inputs(
                    refreshedBalanceManager,
                    ObjectArg.ImmOrOwnedObject.from(
                      accountAddress = AccountAddress.fromString(ourCoin.address),
                      version = ourCoin.version,
                      digest = ourCoin.digest.toString(),
                    ),
                  )
              }
            }
          }

          return maker.sui.signAndExecuteTransactionBlock(maker.owner, balanceManagerDepositPTB)
        }
        is Option.None -> println("No coins found")
      }
    }
  }

  return Option.None
}

internal suspend fun withdraw(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
    amount: ULong,
    type: String,
    receipt: AccountAddress,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    command {
      val withdraw = moveCall {
        target = "${maker.contractAddress()}::balance_manager::withdraw"
        typeArguments = types(Struct.from(type))
        arguments = inputs(refreshedBalanceManager, amount)
      }
      transferObjects {
        objects = inputs(withdraw)
        to = input(receipt)
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun withdrawAll(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
    type: String,
    receipt: AccountAddress,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    command {
      val withdraw = moveCall {
        target = "${maker.contractAddress()}::balance_manager::withdraw_all"
        typeArguments = types(Struct.from(type))
        arguments = inputs(refreshedBalanceManager)
      }
      transferObjects {
        objects = inputs(withdraw)
        to = input(receipt)
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun validateProof(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    val refreshedBalanceManagerArg = inputs(refreshedBalanceManager)

    command {
      val proof = moveCall {
        target = "${maker.contractAddress()}::balance_manager::generate_proof_as_owner"
        arguments = refreshedBalanceManagerArg
      }
      moveCall {
        target = "${maker.contractAddress()}::balance_manager::validate_proof"
        arguments = listOf(input(proof))
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}

internal suspend fun balance(
    maker: DeepBookMarketMaker,
    balanceManager: BalanceManager,
    type: String,
): Option.Some<ExecuteTransactionBlockResult> {

  val refreshedBalanceManager = balanceManager.refresh().expect("Could not refresh balance manager")

  val ptb = programmableTx {
    command {
      moveCall {
        target = "${maker.contractAddress()}::balance_manager::balance"
        typeArguments = types(Struct.from(type))
        arguments = inputs(refreshedBalanceManager)
      }
    }
  }

  return maker.sui.signAndExecuteTransactionBlock(maker.owner, ptb)
}
