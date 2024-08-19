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

import xyz.mcxross.ksui.extension.asIdParts
import xyz.mcxross.ksui.extension.toId
import xyz.mcxross.ksui.internal.getMoveFunctionArgTypes
import xyz.mcxross.ksui.model.MoveFunctionArgTypes
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Move

/**
 * Move implementation
 *
 * This class represents the Move API
 */
class Move(val config: SuiConfig) : Move {

  /**
   * Get the argument types of a Move function, based on normalized Type.
   *
   * It takes in a [String] ID in the format of `package_id::module::function`
   *
   * @param id The ID of the Move function to get the argument types for
   * @return An [Option] of nullable [MoveFunctionArgTypes]
   */
  override suspend fun getMoveFunctionArgTypes(id: String): Option<MoveFunctionArgTypes> =
    getMoveFunctionArgTypes(config, id.asIdParts().toId())
}
