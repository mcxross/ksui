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
package xyz.mcxross.ksui.protocol

import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.LatestSuiSystemState
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.ProtocolConfig

/**
 * General interface
 *
 * This interface represents the general API
 */
interface General {

  /**
   * Get the chain identifier
   *
   * @return An [Option] of nullable [String]
   */
  suspend fun getChainIdentifier(): Option<String>

  /**
   * Get the reference gas price
   *
   * @return An [Option] of nullable [String]
   */
  suspend fun getReferenceGasPrice(): Option<String?>

  /**
   * Get a checkpoint
   *
   * @param checkpointId The checkpoint ID to get
   * @return An [Option] of nullable [Checkpoint]
   */
  suspend fun getCheckpoint(checkpointId: CheckpointId? = null): Option<Checkpoint?>

  /**
   * Get the latest Sui system state
   *
   * @return An [Option] of nullable [LatestSuiSystemState]
   */
  suspend fun getLatestSuiSystemState(): Option<LatestSuiSystemState>

  /**
   * Get the protocol config
   *
   * @param protocolVersion The protocol version to get the config for
   * @return An [Option] of nullable [ProtocolConfig]
   */
  suspend fun getProtocolConfig(protocolVersion: Int? = null): Option<ProtocolConfig>
}
