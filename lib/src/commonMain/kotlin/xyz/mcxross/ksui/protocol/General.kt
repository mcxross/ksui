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

import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.GetChainIdentifierQuery
import xyz.mcxross.ksui.generated.GetCheckpointQuery
import xyz.mcxross.ksui.generated.GetCheckpointsQuery
import xyz.mcxross.ksui.generated.GetCurrentEpochQuery
import xyz.mcxross.ksui.generated.GetLatestCheckpointSequenceNumberQuery
import xyz.mcxross.ksui.generated.GetLatestSuiSystemStateQuery
import xyz.mcxross.ksui.generated.GetProtocolConfigQuery
import xyz.mcxross.ksui.generated.GetReferenceGasPriceQuery
import xyz.mcxross.ksui.generated.PaginateCheckpointTransactionBlocksQuery
import xyz.mcxross.ksui.generated.PaginateEpochValidatorsQuery
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.GraphqlQuery
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

/**
 * Defines the general-purpose API for querying chain-wide information.
 *
 * This interface provides methods to fetch data about checkpoints, epochs, gas prices, and the
 * overall system state of the Sui network.
 */
interface General {
  /** The [SuiConfig] object specifying the RPC endpoint and connection settings. */
  val config: SuiConfig

  /**
   * Fetches the first 4 bytes of the chain's genesis checkpoint digest.
   *
   * This identifier is unique to the specific Sui chain.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetChainIdentifierQuery.Data] object with the chain identifier.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getChainIdentifier(): Result<GetChainIdentifierQuery.Data?, SuiError>

  /**
   * Fetches the current reference gas price for the network.
   *
   * The reference gas price is the minimum gas price that will be accepted for a transaction.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetReferenceGasPriceQuery.Data] object with the gas price
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getReferenceGasPrice(): Result<GetReferenceGasPriceQuery.Data?, SuiError>

  /**
   * Fetches the sequence number of the most recently processed checkpoint.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetLatestCheckpointSequenceNumberQuery.Data] object with the
   *   sequence number.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getLatestCheckpointSequenceNumber():
    Result<GetLatestCheckpointSequenceNumberQuery.Data?, SuiError>

  /**
   * Fetches the details of a specific checkpoint.
   *
   * If `checkpointId` is not provided, it defaults to fetching the latest checkpoint.
   *
   * @param checkpointId The identifier of the checkpoint to fetch. Can be a sequence number or
   *   digest.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetCheckpointQuery.Data] object with the checkpoint details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getCheckpoint(
    checkpointId: CheckpointId? = CheckpointId()
  ): Result<GetCheckpointQuery.Data?, SuiError>

  /**
   * Fetches a paginated list of checkpoints.
   *
   * This method supports bidirectional pagination.
   *
   * @param first An optional integer specifying the number of items to return when paginating
   *   forward.
   * @param after An optional cursor for forward pagination.
   * @param last An optional integer specifying the number of items to return when paginating
   *   backward.
   * @param before An optional cursor for backward pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetCheckpointsQuery.Data] object with a list of checkpoints and
   *   pagination cursors.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getCheckpoints(
    first: Int?,
    before: String?,
    last: Int?,
    after: String?,
  ): Result<GetCheckpointsQuery.Data?, SuiError>

  /**
   * Fetches the most recent `SuiSystemState` object.
   *
   * The system state object contains critical, chain-wide information such as epoch number,
   * protocol version, and validator sets.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetLatestSuiSystemStateQuery.Data] object with the system state
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getLatestSuiSystemState(): Result<GetLatestSuiSystemStateQuery.Data?, SuiError>

  /**
   * Fetches the protocol configuration for a specific version.
   *
   * If no version is provided, the latest protocol configuration is returned.
   *
   * @param protocolVersion An optional integer for the protocol version to query.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetProtocolConfigQuery.Data] object with the configuration
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getProtocolConfig(
    protocolVersion: Int? = null
  ): Result<GetProtocolConfigQuery.Data?, SuiError>

  /**
   * Fetches details about the current epoch.
   *
   * This includes the epoch number, start timestamp, and other epoch-specific data.
   *
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetCurrentEpochQuery.Data] object with the current epoch
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getCurrentEpoch(): Result<GetCurrentEpochQuery.Data?, SuiError>

  /**
   * Fetches a paginated list of transaction blocks for a given checkpoint.
   *
   * @param id The identifier of the checkpoint to query transactions from. Defaults to the latest.
   * @param after An optional cursor for forward pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [PaginateCheckpointTransactionBlocksQuery.Data] object with a
   *   list of transaction blocks and a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun paginateCheckpointTransactionBlocks(
    id: CheckpointId? = null,
    after: String? = null,
  ): Result<PaginateCheckpointTransactionBlocksQuery.Data?, SuiError>

  /**
   * Fetches a paginated list of validators for a specific epoch.
   *
   * @param id The epoch number to fetch the validator set for.
   * @param after An optional cursor for forward pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [PaginateEpochValidatorsQuery.Data] object with a list of
   *   validators and a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun paginateEpochValidators(
    id: Long,
    after: String? = null,
  ): Result<PaginateEpochValidatorsQuery.Data?, SuiError>
}

suspend inline fun <reified T> General.query(query: GraphqlQuery): Nothing = TODO()
