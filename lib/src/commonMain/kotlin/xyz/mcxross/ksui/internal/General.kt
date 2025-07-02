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

package xyz.mcxross.ksui.internal

import com.apollographql.apollo.api.Optional
import xyz.mcxross.ksui.client.getGraphqlClient
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

suspend inline fun <reified T> query(config: SuiConfig, query: GraphqlQuery) {}

internal suspend fun getChainIdentifier(
  config: SuiConfig
): Result<GetChainIdentifierQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetChainIdentifierQuery()) }.toResult()

internal suspend fun getReferenceGasPrice(
  config: SuiConfig
): Result<GetReferenceGasPriceQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetReferenceGasPriceQuery()) }.toResult()

internal suspend fun getCurrentEpoch(
  config: SuiConfig
): Result<GetCurrentEpochQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetCurrentEpochQuery()) }.toResult()

internal suspend fun getLatestCheckpointSequenceNumber(
  config: SuiConfig
): Result<GetLatestCheckpointSequenceNumberQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetLatestCheckpointSequenceNumberQuery()) }.toResult()

internal suspend fun getCheckpoint(
  config: SuiConfig,
  id: CheckpointId?,
): Result<GetCheckpointQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config).query(GetCheckpointQuery(Optional.presentIfNotNull(id?.toGenerated())))
    }
    .toResult()

internal suspend fun getCheckpoints(
  config: SuiConfig,
  first: Int?,
  before: String?,
  last: Int?,
  after: String?,
): Result<GetCheckpointsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetCheckpointsQuery(
            first = Optional.presentIfNotNull(first),
            before = Optional.presentIfNotNull(before),
            last = Optional.presentIfNotNull(last),
            after = Optional.presentIfNotNull(after),
          )
        )
    }
    .toResult()

internal suspend fun getLatestSuiSystemState(
  config: SuiConfig
): Result<GetLatestSuiSystemStateQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetLatestSuiSystemStateQuery()) }.toResult()

internal suspend fun getProtocolConfig(
  config: SuiConfig,
  protocolVersion: Int?,
): Result<GetProtocolConfigQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(GetProtocolConfigQuery(protocolVersion = Optional.presentIfNotNull(protocolVersion)))
    }
    .toResult()

internal suspend fun paginateCheckpointTransactionBlocks(
  config: SuiConfig,
  id: CheckpointId?,
  after: String?,
): Result<PaginateCheckpointTransactionBlocksQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          PaginateCheckpointTransactionBlocksQuery(
            id = Optional.presentIfNotNull(id?.toGenerated()),
            Optional.presentIfNotNull(after),
          )
        )
    }
    .toResult()

internal suspend fun paginateEpochValidators(
  config: SuiConfig,
  id: Long,
  after: String?,
): Result<PaginateEpochValidatorsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(PaginateEpochValidatorsQuery(id, after = Optional.presentIfNotNull(after)))
    }
    .toResult()
