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
@file:OptIn(ExperimentalObjCName::class)

package xyz.mcxross.ksui

import kotlin.concurrent.Volatile
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import xyz.mcxross.ksui.client.withClientInitLock
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.protocol.Coin
import xyz.mcxross.ksui.protocol.Events
import xyz.mcxross.ksui.protocol.Faucet
import xyz.mcxross.ksui.protocol.General
import xyz.mcxross.ksui.protocol.Governance
import xyz.mcxross.ksui.protocol.Move
import xyz.mcxross.ksui.protocol.Object
import xyz.mcxross.ksui.protocol.Sns
import xyz.mcxross.ksui.protocol.Transaction

@ObjCName(swiftName = "SuiKitDefaults")
object SuiKit {
  @Volatile private var default: Sui? = null

  /**
   * The client that `ptb {}`, `compose` and the sponsored-transaction helpers use when none is
   * passed. Every `Sui(makeDefault = true)` replaces it, so code that talks to more than one
   * network should pass its client explicitly. Closing the default client clears it.
   */
  val client: Sui
    get() =
      default
        ?: throw IllegalStateException(
          "No default Sui client: create Sui(config) with makeDefault = true, or pass a client",
        )

  internal fun setDefault(client: Sui) {
    withClientInitLock { default = client }
  }

  internal fun clearDefault(client: Sui) {
    withClientInitLock { if (default === client) default = null }
  }
}

class Sui(config: SuiConfig = SuiConfig(), makeDefault: Boolean = true) :
  AutoCloseable,
  Coin by xyz.mcxross.ksui.api.Coin(config),
  Events by xyz.mcxross.ksui.api.Events(config),
  Governance by xyz.mcxross.ksui.api.Governance(config),
  General by xyz.mcxross.ksui.api.General(config),
  Transaction by xyz.mcxross.ksui.api.Transaction(config),
  Object by xyz.mcxross.ksui.api.Object(config),
  Sns by xyz.mcxross.ksui.api.Sns(config),
  Move by xyz.mcxross.ksui.api.Move(config),
  Faucet by xyz.mcxross.ksui.api.Faucet(config) {
  init {
    if (makeDefault) {
      SuiKit.setDefault(this)
    }
  }

  override fun close() {
    // A closed default would otherwise fail later, deep inside a request.
    SuiKit.clearDefault(this)
    config.close()
  }
}
