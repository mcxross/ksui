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
package xyz.mcxross.ksui.internal

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.inputs

internal fun moveCall(
  target: String,
  typeArguments: List<TypeTag> = emptyList(),
  args: List<Argument> = emptyList(),
): ProgrammableTransaction = ptb {
  moveCall {
    this.target = target
    this.typeArguments = typeArguments
    this.arguments = args
  }
}

internal fun splitCoin(coin: Argument, amounts: List<Long>): ProgrammableTransaction = ptb {
  splitCoins {
    this.coin = coin
    this.into = inputs(amounts)
  }
}

internal fun transferObject(objs: List<Argument>, to: AccountAddress): ProgrammableTransaction =
  ptb {
    transferObjects {
      this.objects = inputs(objs)
      this.to = input(to)
    }
  }
