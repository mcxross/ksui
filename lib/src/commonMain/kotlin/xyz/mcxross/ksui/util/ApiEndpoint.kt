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

package xyz.mcxross.ksui.util

import xyz.mcxross.ksui.model.Network

val NetworkToIndexerAPI =
  mapOf(
    "mainnet" to "https://sui-mainnet.mystenlabs.com/graphql",
    "testnet" to "https://sui-testnet.mystenlabs.com/graphql",
    "devnet" to "https://sui-devnet.mystenlabs.com/graphql",
    "local" to "http://127.0.0.1:8090/v1/graphql",
  )

val NetworkToNodeAPI =
  mapOf(
    "mainnet" to "https://fullnode.mainnet.sui.io:443",
    "testnet" to "https://fullnode.testnet.sui.io:443",
    "devnet" to "https://fullnode.devnet.sui.io:443",
    "local" to "http://0.0.0.0:9000",
  )

val NetworkToFaucetAPI =
  mapOf(
    "mainnet" to "https://faucet.mainnet.sui.io/gas",
    "testnet" to "https://faucet.testnet.sui.io/gas",
    "devnet" to "https://faucet.devnet.sui.io/gas",
    "local" to "http://127.0.0.1:5003/gas",
  )

val NetworkToChainId = mapOf("mainnet" to 1, "testnet" to 2, "randomnet" to 70)

val NetworkToNetworkName =
  mapOf(
    "mainnet" to Network.MAINNET,
    "testnet" to Network.TESTNET,
    "devnet" to Network.DEVNET,
    "custom" to Network.CUSTOM,
  )
