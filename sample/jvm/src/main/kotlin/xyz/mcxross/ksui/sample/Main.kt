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
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings

suspend fun main() {

  // Create a new instance of Sui with the testnet network. Defaults to DEVNET if not specified.
  val sui = Sui(config = SuiConfig(settings = SuiSettings(Network.TESTNET)))

  println(ALICE_ACCOUNT.address.toString())

  val committeeInfo = sui.getCommitteeInfo()

  println("Committee Info for current epoch: $committeeInfo")
}
