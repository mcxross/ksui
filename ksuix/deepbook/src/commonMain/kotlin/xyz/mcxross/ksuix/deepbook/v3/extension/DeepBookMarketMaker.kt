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
package xyz.mcxross.ksuix.deepbook.v3.extension

import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksuix.deepbook.v3.DeepBookMarketMaker
import xyz.mcxross.ksuix.deepbook.v3.util.DEEPBOOK_V3_CONTRACT_ADDRESS_TESTNET

fun DeepBookMarketMaker.contractAddress(): String {
  return when (sui.config.network) {
    Network.DEVNET -> throw Exception("Not yet implemented")
    Network.TESTNET -> DEEPBOOK_V3_CONTRACT_ADDRESS_TESTNET
    Network.MAINNET -> throw Exception("Not yet implemented")
    else -> throw Exception("Unsupported network")
  }
}
