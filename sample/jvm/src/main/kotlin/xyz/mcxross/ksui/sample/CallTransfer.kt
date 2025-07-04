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

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.runBlocking

const val TARGET =
  "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::new_account"

fun main() = runBlocking {
  val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))

  println("Alice's address: ${ALICE_ACCOUNT.address}")
  println("Bob's address: ${BOB_ACCOUNT.address}")

  val ptb = ptb {
    val acc = moveCall {
      target = TARGET
      arguments =
        listOf(
          `object`("0x01327a5e0ae0617d09d3151a2d4db4112f0b910221d457e8747253de32e70b17"),
          `object`("0x698bc414f25a7036d9a72d6861d9d268e478492dc8bfef8b5c1c2f1eae769254"),
        )
    }

    transferObjects {
      objects = listOf(acc)
      to = address(BOB_ACCOUNT.address)
    }
  }

  val res =
    sui
      .signAndExecuteTransactionBlock(
        signer = ALICE_ACCOUNT,
        ptb = ptb,
        gasBudget = 15_000_000UL,
        options = ExecuteTransactionBlockResponseOptions(showObjectChanges = true, showEffects = true),
      )
      .expect { "Transaction Execution Failed" }

  println(
    "============================================<<<>>>============================================"
  )
  println("Execution Response: $res")
}
