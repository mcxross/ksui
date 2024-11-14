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
package xyz.mcxross.ksui.unit

import kotlin.test.Test
import kotlin.test.assertTrue
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.Command
import xyz.mcxross.ksui.ptb.programmableTx
import xyz.mcxross.ksui.util.inputs

const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

class CommandTest {

  @Test
  fun testCommandGeneration() {
    val ptb = programmableTx { command { moveCall { target = HELLO_WORLD } } }
    assertTrue { ptb.commands.size == 1 }
    assertTrue { ptb.inputs.isEmpty() }
  }

  @Test
  fun testMoveCall() {
    val ptb = programmableTx { command { moveCall { target = HELLO_WORLD } } }
    val moveCallCommand = ptb.commands[0] as Command.MoveCall
    val moveCall = moveCallCommand.moveCall
    assertTrue { ptb.commands[0] is Command.MoveCall }
    assertTrue { moveCall.arguments.isEmpty() }
    assertTrue {
      moveCall.pakage.toString() ==
        "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d"
    }
    assertTrue { moveCall.module == "hello_wolrd" }
    assertTrue { moveCall.function == "hello_world" }
    assertTrue { ptb.inputs.isEmpty() }
  }

  @Test
  fun testTransferObjects() {
    val ptb = programmableTx {
      command {
        transferObjects {
          objects = inputs("0x1234567890", "0x0987654321")
          to = input(AccountAddress.EMPTY)
        }
      }
    }
    val transferObjectsCommand = ptb.commands[0] as Command.TransferObjects
    val transferObjects = transferObjectsCommand.objects
    assertTrue { ptb.commands[0] is Command.TransferObjects }
    assertTrue { transferObjects.size == 2 }
  }

  @Test
  fun testSplitCoins() {
    val ptb = programmableTx {
      command {
        val splitCoins = splitCoins {
          coin = Argument.GasCoin
          into = inputs(1_000_000UL, 2_000_000UL, 3_000_000UL)
        }
        transferObjects {
          objects = inputs(splitCoins)
          to = input(AccountAddress.EMPTY)
        }
      }
    }

    val splitCoinsCommand = ptb.commands[0] as Command.SplitCoins
    val coin = splitCoinsCommand.coin
    val into = splitCoinsCommand.into
    assertTrue { ptb.commands[0] is Command.SplitCoins }
    assertTrue { coin == Argument.GasCoin }
    assertTrue { into.size == 3 }
  }

  @Test
  fun testPTB() {
    val ptb = programmableTx {
      command {
        val splitCoins = splitCoins {
          coin = Argument.GasCoin
          into = inputs(1, 2, 3)
        }
        val transferObjects = transferObjects {
          this.objects = inputs(splitCoins)
          to = input(AccountAddress.EMPTY)
        }
      }
    }
  }
}
