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

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.runBlocking

fun main() = runBlocking {
  val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))

  val ptb = ptb {
    val ramp = `object`("0x67a60352909987c0a3777d15444e883599ee8799c742aa97b6a23205da29867a")

    val auth = moveCall {
      target =
        "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::authenticate"
      arguments = +ramp
    }

    val params = moveCall {
      target =
        "0x10c87c29ea5d5674458652ababa246742a763f9deafed11608b7f0baea296484::intents::new_params"
      arguments =
        pure(randomString(10)) + pure("description") + pure(listOf(0UL)) + pure(900000UL) + clock()
    }

    val outcome = moveCall {
      target =
        "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::empty_approved_outcome"
    }

    val members =
      arg(
        listOf(
          "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a904",
          "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a902",
        ),
        TypeTag.Address,
      )

    moveCall {
      target =
        "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::config::request_config_p2p_ramp"
      arguments = auth + params + outcome + ramp + members
    }
  }

  val res =
    sui
      .signAndExecuteTransactionBlock(
        signer = ALICE_ACCOUNT,
        ptb = ptb,
        gasBudget = 15_000_000UL,
        options =
          ExecuteTransactionBlockResponseOptions(showObjectChanges = true, showEffects = true),
      )
      .expect { "Transaction Execution Failed" }

  println(
    "============================================<<<>>>============================================"
  )
  println("Execution Response: $res")
}
