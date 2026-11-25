/*
 * Copyright 2026 McXross
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

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Subscription
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.mcxross.ksui.client.getSharedGraphqlSubscriptionClient
import xyz.mcxross.ksui.core.exception.ErrorLocation
import xyz.mcxross.ksui.core.exception.SdkErrorDetail
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig

internal fun <D : Subscription.Data> subscriptionResults(
  config: SuiConfig,
  operation: Subscription<D>,
): Flow<Result<D?, SuiError>> =
  subscriptionResults(
    clientFactory = { getSharedGraphqlSubscriptionClient(config) },
    operation = operation,
    closeOnFinish = false,
  )

internal fun <D : Subscription.Data> subscriptionResults(
  clientFactory: () -> ApolloClient,
  operation: Subscription<D>,
  closeOnFinish: Boolean = true,
): Flow<Result<D?, SuiError>> = flow {
  val client = clientFactory()
  try {
    client.subscription(operation).toFlow().collect { emit(it.toSuiResult()) }
  } catch (e: CancellationException) {
    throw e
  } catch (e: ApolloException) {
    emit(
      Result.Err(
        SuiError(listOf(SdkErrorDetail(message = "GraphQL subscription failed: ${e.message}")))
      )
    )
  } finally {
    if (closeOnFinish) {
      client.close()
    }
  }
}

private fun <D : Subscription.Data> ApolloResponse<D>.toSuiResult(): Result<D?, SuiError> {
  errors?.takeIf { it.isNotEmpty() }?.let { graphQLErrors ->
    return Result.Err(
      SuiError(
        graphQLErrors.map {
          SdkErrorDetail(
            message = it.message,
            locations = it.locations?.map { loc -> ErrorLocation(loc.line, loc.column) },
            path = it.path,
            extensions = it.extensions,
          )
        }
      )
    )
  }
  exception?.let { ex ->
    return Result.Err(
      SuiError(listOf(SdkErrorDetail(message = "GraphQL subscription failed: ${ex.message}")))
    )
  }
  data?.let { return Result.Ok(it) }
  return Result.Err(
    SuiError(
      listOf(
        SdkErrorDetail(
          message = "Unknown error: no data and no errors returned from GraphQL server"
        )
      )
    )
  )
}
