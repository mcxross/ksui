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
import xyz.mcxross.ksui.protocol.Faucet
import xyz.mcxross.ksui.protocol.General
import xyz.mcxross.ksui.protocol.Governance
import xyz.mcxross.ksui.protocol.Move
import xyz.mcxross.ksui.protocol.Object
import xyz.mcxross.ksui.protocol.Sns
import xyz.mcxross.ksui.protocol.Transaction

/**
 * Sui is a class that implements all the interfaces of the Sui API.
 *
 * It is the entry point for all the API calls and related operations.
 */
class Sui(config: SuiConfig = SuiConfig()) :
  Coin by xyz.mcxross.ksui.api.Coin(config),
  Governance by xyz.mcxross.ksui.api.Governance(config),
  General by xyz.mcxross.ksui.api.General(config),
  Transaction by xyz.mcxross.ksui.api.Transaction(config),
  Object by xyz.mcxross.ksui.api.Object(config),
  Sns by xyz.mcxross.ksui.api.Sns(config),
  Move by xyz.mcxross.ksui.api.Move(config),
  Faucet by xyz.mcxross.ksui.api.Faucet(config)
