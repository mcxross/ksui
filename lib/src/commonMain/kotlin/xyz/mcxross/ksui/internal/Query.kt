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

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloException
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError

internal suspend fun <D : Operation.Data> handleQuery(
  queryCall: suspend () -> ApolloCall<D>
): Result<D?, SuiError> {
  try {
    val response = queryCall().execute()

    if (response.data != null) {
      return Ok(response.data)
    }

    val errors = response.errors
    if (!errors.isNullOrEmpty()) {
      return Err(SuiError.from(errors))
    } else {
      SuiError(
        listOf(
          GraphQLError(
            message = "GraphQL errors in response"
          )
        )
      )
    }

    return Err(
      SuiError(
        listOf(
          GraphQLError(
            message = "Unknown error: no data and no errors returned from GraphQL server"
          )
        )
      )
    )
  } catch (e: ApolloException) {
    return Err(
      SuiError(listOf(GraphQLError(message = "GraphQL client request failed: ${e.message}")))
    )
  }
}
