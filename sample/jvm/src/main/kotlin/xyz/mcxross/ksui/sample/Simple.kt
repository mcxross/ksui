/*
 * Copyright 2025 McXross
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
package xyz.mcxross.ksui.sample

import kotlinx.coroutines.runBlocking
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.ptb

fun main() = runBlocking {
  val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))

  val ptb = ptb {
    val coins = splitCoins {
      coin = Argument.GasCoin
      into = +pure(1_000_000UL)
    }

    transferObjects {
      objects = coins
      to = arg(ALICE_ACCOUNT.address)
    }
  }

  val res =
    sui.signAndExecuteTransactionBlock(signer = ALICE_ACCOUNT, ptb = ptb).expect {
      "Transaction Execution Failed"
    }
  println(res)
  val waited = sui.waitForTransaction(res?.executeTransaction?.effects?.transaction?.digest!!)

  when (waited) {
    is Result.Ok -> println("Transaction executed successfully")
    is Result.Err -> println("Transaction failed with error: ${waited.error}")
  }

  println(waited)
}
