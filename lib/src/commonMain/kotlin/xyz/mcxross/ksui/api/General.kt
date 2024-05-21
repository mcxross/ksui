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

import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.General

class General(val config: SuiConfig) : General {
  override suspend fun getChainIdentifier(): Option<String> =
    xyz.mcxross.ksui.internal.getChainIdentifier(config)

  override suspend fun getReferenceGasPrice(): Option<String?> =
    xyz.mcxross.ksui.internal.getReferenceGasPrice(config)

  override suspend fun getCheckpoint(checkpointId: CheckpointId?): Option<Checkpoint?> =
    xyz.mcxross.ksui.internal.getCheckpoint(config, checkpointId)
}
