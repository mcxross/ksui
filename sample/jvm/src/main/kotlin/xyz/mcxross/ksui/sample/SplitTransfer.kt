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
package xyz.mcxross.ksui.sample

import kotlinx.coroutines.delay
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.runBlocking

data class TransferInfo(val recipient: String, val amount: ULong)

fun main() = runBlocking {
  val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))

  println("Bob's address: ${BOB_ACCOUNT.address}")
  println("Carol's address: ${CAROL_ACCOUNT.address}")

  val transfers =
    listOf(
      TransferInfo(recipient = BOB_ACCOUNT.address.toString(), amount = 100_000_000UL),
      TransferInfo(recipient = CAROL_ACCOUNT.address.toString(), amount = 200_000_000UL),
    )

  val ptb = ptb {
    val coins = splitCoins {
      coin = Argument.GasCoin
      into = transfers.map { pure(it.amount) }
    }

    transfers.forEachIndexed { index, transfer ->
      transferObjects {
        objects = listOf(coins[index])
        to = address(transfer.recipient)
      }
    }
  }

  val res =
    sui.signAndExecuteTransactionBlock(signer = ALICE_ACCOUNT, ptb = ptb).expect {
      "Transaction Execution Failed"
    }

  println(
    "============================================<<<>>>============================================"
  )
  println(res)
  println(
    "============================================<<<>>>============================================"
  )

  val bobBalance = sui.getBalance(BOB_ACCOUNT.address).expect { "Couldn't retrieve Bob's balance" }
  val carolBalance =
    sui.getBalance(CAROL_ACCOUNT.address).expect { "Couldn't retrieve Carol's balance" }

  println("Bob's Balance after tx: $bobBalance")
  delay(3000)
  println("Carols's Balance after tx: $carolBalance")
}
