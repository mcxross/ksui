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

fun main() = runBlocking {
  val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))

  println("Alice's address: ${ALICE_ACCOUNT.address}")

  val ptb = ptb {
    moveCall {
      target = HELLO_WORLD
      arguments = listOf(pure(0UL))
    }
  }

  val res =
    sui.signAndExecuteTransactionBlock(
      signer = ALICE_ACCOUNT,
      ptb = ptb,
      options = ExecuteTransactionBlockResponseOptions(showEffects = true, showObjectChanges = true),
    )

  println(
    "============================================<<<>>>============================================"
  )
  println("Execution Response: $res")
}
