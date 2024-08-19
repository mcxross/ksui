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

import io.ktor.client.call.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.client.postSuiIndexer
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.generated.GetChainIdentifier
import xyz.mcxross.ksui.generated.GetCheckpoint
import xyz.mcxross.ksui.generated.GetLatestSuiSystemState
import xyz.mcxross.ksui.generated.GetProtocolConfig
import xyz.mcxross.ksui.generated.GetReferenceGasPrice
import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.GraphqlQuery
import xyz.mcxross.ksui.model.LatestSuiSystemState
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.ProtocolConfig
import xyz.mcxross.ksui.model.RequestOptions
import xyz.mcxross.ksui.model.SuiConfig

suspend inline fun <reified T> query(config: SuiConfig, query: GraphqlQuery): T {
  val response =
    postSuiIndexer(
      RequestOptions.PostSuiRequestOptions(suiConfig = config, body = Json.encodeToString(query))
    )

  if (response.status.value in 400..599) {
    throw SuiException("Failed to execute query: ${response.status}")
  }

  return response.body()
}

internal suspend fun getChainIdentifier(config: SuiConfig): Option<String> {
  val client = getGraphqlClient(config)
  val response = client.execute(GetChainIdentifier())

  if (response.errors != null) {
    throw Exception(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!.chainIdentifier)
}

internal suspend fun getReferenceGasPrice(config: SuiConfig): Option<String?> {
  val client = getGraphqlClient(config)
  val response = client.execute<GetReferenceGasPrice.Result>(GetReferenceGasPrice())

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!.epoch?.referenceGasPrice)
}

internal suspend fun getCheckpoint(
  config: SuiConfig,
  checkpoint: CheckpointId?,
): Option<Checkpoint?> {
  val client = getGraphqlClient(config)
  val response =
    client.execute<GetCheckpoint.Result>(
      GetCheckpoint(
        variables =
          GetCheckpoint.Variables(
            xyz.mcxross.ksui.generated.inputs.CheckpointId(
              checkpoint?.digest,
              checkpoint?.sequenceNumber?.toInt(),
            )
          )
      )
    )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!.checkpoint)
}

internal suspend fun getLatestSuiSystemState(config: SuiConfig): Option<LatestSuiSystemState?> {
  val client = getGraphqlClient(config)
  val response = client.execute<GetLatestSuiSystemState.Result>(GetLatestSuiSystemState())

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!)
}

internal suspend fun getProtocolConfig(
  config: SuiConfig,
  protocolVersion: Int?,
): Option<ProtocolConfig> {
  val client = getGraphqlClient(config)
  val response =
    client.execute<GetProtocolConfig.Result>(
      GetProtocolConfig(variables = GetProtocolConfig.Variables(protocolVersion))
    )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data!!)
}
