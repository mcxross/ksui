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
package xyz.mcxross.ksui.api

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.protocol.Extended
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.ProgrammableTransaction

class Extended(val config: SuiConfig) : Extended {
  override fun moveCall(
    target: String,
    typeArguments: List<TypeTag>,
    args: List<Argument>,
  ): ProgrammableTransaction = xyz.mcxross.ksui.internal.moveCall(target, typeArguments, args)

  override fun splitCoin(coin: Argument, amounts: List<Long>): ProgrammableTransaction =
    xyz.mcxross.ksui.internal.splitCoin(coin, amounts)

  override fun transferObject(objs: List<Argument>, to: AccountAddress): ProgrammableTransaction =
    xyz.mcxross.ksui.internal.transferObject(objs, to)
}
