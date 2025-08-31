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
package xyz.mcxross.ksui

import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Coin
import xyz.mcxross.ksui.protocol.Events
import xyz.mcxross.ksui.protocol.Faucet
import xyz.mcxross.ksui.protocol.General
import xyz.mcxross.ksui.protocol.Governance
import xyz.mcxross.ksui.protocol.Move
import xyz.mcxross.ksui.protocol.Object
import xyz.mcxross.ksui.protocol.Sns
import xyz.mcxross.ksui.protocol.Transaction

/**
 * A singleton object that holds a default, globally accessible `Sui` client instance.
 *
 * This pattern simplifies the API by allowing functions like `ptb` to work without explicitly
 * passing a client instance for every call, making the syntax cleaner (`ptb { ... }`).
 *
 * The default client is automatically set when the first `Sui` object is instantiated in an
 * application. This behavior can be opted out of on a per-instance basis if needed.
 *
 * @property client The globally configured default `Sui` client. Throws an
 *   [UninitializedPropertyAccessException] if accessed before a `Sui` instance is created.
 */
object SuiKit {
  lateinit var client: Sui
    private set

  internal fun setDefault(client: Sui) {
    if (!::client.isInitialized) {
      this.client = client
    }
  }
}

/**
 * The primary entry point for interacting with the Sui network.
 *
 * This class provides a unified API surface by delegating to various specialized API clients (e.g.,
 * `Coin`, `Events`, `Transaction`). This composition pattern allows for clean separation of
 * concerns while offering a simple, cohesive interface.
 *
 * Upon instantiation, this class can automatically configure a default, globally-accessible client
 * instance via the [SuiKit] object. This enables the use of convenient top-level functions like
 * `ptb { ... }` without needing to pass the client instance explicitly.
 *
 * @param config The [SuiConfig] used to configure the connection and behavior of all underlying API
 *   clients.
 * @param makeDefault If `true`, this instance will be set as the default client in [SuiKit], making
 *   it available for global functions. This is useful for setting up a primary client at
 *   application startup. Set to `false` to create a temporary or secondary client without
 *   overwriting the default.
 */
class Sui(config: SuiConfig = SuiConfig(), makeDefault: Boolean = true) :
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
}
