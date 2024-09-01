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
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.ptb.programmableTx
import xyz.mcxross.ksui.util.inputs
import xyz.mcxross.ksui.util.runBlocking

const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

fun main() = runBlocking {

  // Create a new Sui instance with the testnet network
  val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))

  // Create a new account
  val alice = Account.create()

  println("Created account: ${alice.address}")

  // Let's request some testnet coins
  val transferredGasObjs = sui.requestTestTokens(alice.address)

  println("Transferred gas: $transferredGasObjs")

  // Programmable transaction block
  val ptb = programmableTx {
    command {
      moveCall {
        target = HELLO_WORLD
        arguments = inputs(0UL)
      }
    }
  }

  // Sign and execute the transaction block
  val res = sui.signAndExecuteTransactionBlock(signer = alice, ptb = ptb)

  println(res)
}
