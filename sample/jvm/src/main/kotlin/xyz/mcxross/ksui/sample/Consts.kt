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

import xyz.mcxross.ksui.core.account.Account

private fun requiredEnvironment(name: String): String =
  requireNotNull(System.getenv(name)?.takeIf { it.isNotBlank() }) {
    "$name must contain an explicitly disposable testnet credential"
  }

private fun sampleAccount(name: String): Account = Account.import(requiredEnvironment(name))

val ALICE_ACCOUNT: Account by lazy { sampleAccount("KSUI_SAMPLE_ALICE_KEY") }

val BOB_ACCOUNT: Account by lazy { sampleAccount("KSUI_SAMPLE_BOB_KEY") }

val CAROL_ACCOUNT: Account by lazy { sampleAccount("KSUI_SAMPLE_CAROL_KEY") }

const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

val GAS_STATION_URL: String by lazy { requiredEnvironment("GAS_STATION_URL") }
val GAS_STATION_API_KEY: String by lazy { requiredEnvironment("GAS_STATION_API_KEY") }
