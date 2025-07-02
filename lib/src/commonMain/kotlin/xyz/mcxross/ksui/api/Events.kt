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

import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.QueryEventsQuery
import xyz.mcxross.ksui.internal.queryEvents
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Events

/**
 * The concrete implementation of the [Events] interface.
 *
 * @param config The [SuiConfig] object specifying the RPC endpoint and connection settings.
 */
class Events(val config: SuiConfig) : Events {
  /**
   * Queries for events on the Sui network based on a specified filter.
   *
   * This method supports bidirectional pagination.
   *
   * @param filter The [EventFilter] to apply to the query, such as filtering by sender or move
   *   module.
   * @param before An optional cursor for backward pagination. Fetches the page of events before
   *   this cursor.
   * @param after An optional cursor for forward pagination. Fetches the page of events after this
   *   cursor.
   * @param first An optional integer specifying the number of events to return when paginating
   *   forward (used with `after`).
   * @param last An optional integer specifying the number of events to return when paginating
   *   backward (used with `before`).
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [QueryEventsQuery.Data] object. This object includes a list of
   *   events and cursors for pagination.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun queryEvents(
    filter: EventFilter,
    before: String?,
    after: String?,
    first: Int?,
    last: Int?,
  ): Result<QueryEventsQuery.Data?, SuiError> =
    queryEvents(config, filter, before, after, first, last)
}
