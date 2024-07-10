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
import xyz.mcxross.ksui.internal.getChainIdentifier
import xyz.mcxross.ksui.internal.getCheckpoint
import xyz.mcxross.ksui.internal.getLatestSuiSystemState
import xyz.mcxross.ksui.internal.getProtocolConfig
import xyz.mcxross.ksui.internal.getReferenceGasPrice
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.LatestSuiSystemState
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.ProtocolConfig
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.General

class General(val config: SuiConfig) : General {

  /**
   * Get the first 4 bytes of the chain's genesis checkpoint digest.
   *
   * @return An [Option] of nullable [String]
   */
  override suspend fun getChainIdentifier(): Option<String> = getChainIdentifier(config)

  /**
   * Get the reference gas price of the current epoch
   *
   * @return An [Option] of nullable [String]
   */
  override suspend fun getReferenceGasPrice(): Option<String?> = getReferenceGasPrice(config)

  /**
   * Get a checkpoint by ID
   *
   * @param checkpointId The ID of the checkpoint to get
   * @return An [Option] of nullable [Checkpoint]
   */
  override suspend fun getCheckpoint(checkpointId: CheckpointId?): Option<Checkpoint?> =
    getCheckpoint(config, checkpointId)

  /**
   * Get the latest Sui system state
   *
   * @return An [Option] of nullable [LatestSuiSystemState]
   */
  override suspend fun getLatestSuiSystemState(): Option<LatestSuiSystemState> =
    getLatestSuiSystemState(config)

  /**
   * Get the protocol config
   *
   * @param protocolVersion The version of the protocol to get
   * @return An [Option] of nullable [ProtocolConfig]
   */
  override suspend fun getProtocolConfig(protocolVersion: Int?): Option<ProtocolConfig> =
    getProtocolConfig(config, protocolVersion)
}
