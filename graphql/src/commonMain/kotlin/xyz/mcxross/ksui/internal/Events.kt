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
import xyz.mcxross.ksui.generated.QueryEventsQuery
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun queryEvents(
  config: SuiConfig,
  filter: EventFilter,
  before: String?,
  after: String?,
  first: Int?,
  last: Int?,
): Result<QueryEventsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          QueryEventsQuery(
            filter = filter.toGenerated(),
            before = Optional.presentIfNotNull(before),
            after = Optional.presentIfNotNull(after),
            first = Optional.presentIfNotNull(first),
            last = Optional.presentIfNotNull(last),
          )
        )
    }
    .toResult()
