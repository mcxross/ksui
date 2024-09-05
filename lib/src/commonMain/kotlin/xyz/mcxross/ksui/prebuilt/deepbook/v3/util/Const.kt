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
package xyz.mcxross.ksui.prebuilt.deepbook.v3.util

import xyz.mcxross.ksui.prebuilt.deepbook.v3.model.Coin
import xyz.mcxross.ksui.prebuilt.deepbook.v3.model.Market

const val DEEPBOOK_V3_CONTRACT_ADDRESS_MAINNET = ""
const val DEEPBOOK_V3_CONTRACT_ADDRESS_TESTNET =
  "0x3228ce0225baacb211e230f74891ceaabe3edeae230090ea3a5a176e535af7e9"
const val DEEPBOOK_V3_CONTRACT_ADDRESS_DEVNET = ""

const val DEEP_SUI = "DEEP_SUI"
const val SUI_DBUSDC = "SUI_DBUSDC"
const val DEEP_DBUSDC = "DEEP_DBUSDC"
const val DBUSDT_DBUSDC = "DBUSDT_DBUSDC"

val testnetCoins: Map<String, Coin> =
  mapOf(
    "DEEP" to
      Coin(
        address = "0x36dbef866a1d62bf7328989a10fb2f07d769f4ee587c0de4a0a256e57e0a58a8",
        type = "0x36dbef866a1d62bf7328989a10fb2f07d769f4ee587c0de4a0a256e57e0a58a8::deep::DEEP",
        scalar = 1000000,
      ),
    "SUI" to
      Coin(
        address = "0x0000000000000000000000000000000000000000000000000000000000000002",
        type = "0x0000000000000000000000000000000000000000000000000000000000000002::sui::SUI",
        scalar = 1000000000,
      ),
    "DBUSDC" to
      Coin(
        address = "0xf7152c05930480cd740d7311b5b8b45c6f488e3a53a11c3f74a6fac36a52e0d7",
        type = "0xf7152c05930480cd740d7311b5b8b45c6f488e3a53a11c3f74a6fac36a52e0d7::DBUSDC::DBUSDC",
        scalar = 1000000,
      ),
    "DBUSDT" to
      Coin(
        address = "0xf7152c05930480cd740d7311b5b8b45c6f488e3a53a11c3f74a6fac36a52e0d7",
        type = "0xf7152c05930480cd740d7311b5b8b45c6f488e3a53a11c3f74a6fac36a52e0d7::DBUSDT::DBUSDT",
        scalar = 1000000,
      ),
  )

val testnetMarkets: Map<String, Market> =
  mapOf(
    DEEP_SUI to
      Market(
        address = "0x2decc59a6f05c5800e5c8a1135f9d133d1746f562bf56673e6e81ef4f7ccd3b7",
        baseAsset = testnetCoins["DEEP"]!!,
        quoteAsset = testnetCoins["SUI"]!!,
      ),
    SUI_DBUSDC to
      Market(
        address = "0xace543e8239f0c19783e57bacb02c581fd38d52899bdce117e49c91b494c8b10",
        baseAsset = testnetCoins["SUI"]!!,
        quoteAsset = testnetCoins["DBUSDC"]!!,
      ),
    DEEP_DBUSDC to
      Market(
        address = "0x1faaa544a84c16215ef005edb046ddf8e1cfec0792aec3032e86e554b33bd33a",
        baseAsset = testnetCoins["DEEP"]!!,
        quoteAsset = testnetCoins["DBUSDC"]!!,
      ),
    DBUSDT_DBUSDC to
      Market(
        address = "0x83aca040eaeaf061e3d482a44d1a87a5b8b6206ad52edae9d0479b830a38106f",
        baseAsset = testnetCoins["DBUSDT"]!!,
        quoteAsset = testnetCoins["DBUSDC"]!!,
      ),
  )
