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

package xyz.mcxross.ksui.model

import com.apollographql.apollo.api.Optional
import xyz.mcxross.ksui.generated.type.EventFilter

data class EventFilter(
  val sender: String? = null,
  val transactionDigest: String? = null,
  val emittingModule: String? = null,
  val eventType: String? = null,
) {
  fun toGenerated(): EventFilter {
    return EventFilter(
      sender = Optional.presentIfNotNull(sender),
      transactionDigest = Optional.presentIfNotNull(transactionDigest),
      emittingModule = Optional.presentIfNotNull(emittingModule),
      eventType = Optional.presentIfNotNull(eventType),
    )
  }
}
