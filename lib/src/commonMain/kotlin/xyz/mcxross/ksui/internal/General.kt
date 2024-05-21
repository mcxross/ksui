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

import xyz.mcxross.graphql.client.DefaultGraphQLClient
import xyz.mcxross.ksui.generated.DryRunTransaction
import xyz.mcxross.ksui.generated.GetChainIdentifier
import xyz.mcxross.ksui.generated.GetCheckpoint
import xyz.mcxross.ksui.generated.GetReferenceGasPrice
import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getChainIdentifier(config: SuiConfig): Option<String> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response = client.execute(GetChainIdentifier())

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.chainIdentifier)
}

internal suspend fun getReferenceGasPrice(config: SuiConfig): Option<String?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response = client.execute<GetReferenceGasPrice.Result>(GetReferenceGasPrice())

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.epoch?.referenceGasPrice)
}

internal suspend fun getCheckpoint(
  config: SuiConfig,
  checkpoint: CheckpointId?,
): Option<Checkpoint?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response =
    client.execute<GetCheckpoint.Result>(
      GetCheckpoint(
        GetCheckpoint.Variables(checkpoint?.digest, checkpoint?.sequenceNumber?.toInt())
      )
    )

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.checkpoint)
}

internal suspend fun dryRunTransaction(config: SuiConfig, transaction: String): Option<String> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response =
    client.execute<DryRunTransaction.Result>(
      DryRunTransaction(DryRunTransaction.Variables(transaction))
    )

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.dryRunTransactionBlock.error ?: "")
}
