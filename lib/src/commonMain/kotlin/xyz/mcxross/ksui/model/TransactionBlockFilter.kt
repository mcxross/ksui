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
import xyz.mcxross.ksui.generated.type.TransactionFilter

data class TransactionBlockFilter(
  val afterCheckpoint: String? = null,
  val atCheckpoint: String? = null,
  val beforeCheckpoint: String? = null,
  val function: String? = null,
  val kind: TransactionBlockKindInput? = null,
  val affectedAddress: String? = null,
  val affectedObjects: String? = null,
  val sentAddress: String? = null,
) {
  fun toGenerated(): TransactionFilter =
    TransactionFilter(
      afterCheckpoint = Optional.presentIfNotNull(afterCheckpoint),
      atCheckpoint = Optional.presentIfNotNull(atCheckpoint),
      beforeCheckpoint = Optional.presentIfNotNull(beforeCheckpoint),
      function = Optional.presentIfNotNull(function),
      kind = Optional.presentIfNotNull(kind?.toGenerated()),
      affectedAddress = Optional.presentIfNotNull(affectedAddress),
      sentAddress = Optional.presentIfNotNull(sentAddress),
      affectedObject = Optional.presentIfNotNull(affectedObjects),
    )
}

fun transactionFilter(block: TransactionFilterBuilder.() -> Unit): TransactionBlockFilter {
  return TransactionFilterBuilder().apply(block).build()
}

class TransactionFilterBuilder {
  var afterCheckpoint: String? = null
  var atCheckpoint: String? = null
  var beforeCheckpoint: String? = null
  var function: String? = null
  var kind: TransactionBlockKindInput? = null
  var affectedAddress: String? = null
  var affectedObjects: String? = null
  var sentAddress: String? = null

  fun build(): TransactionBlockFilter =
    TransactionBlockFilter(
      afterCheckpoint = afterCheckpoint,
      atCheckpoint = atCheckpoint,
      beforeCheckpoint = beforeCheckpoint,
      function = function,
      kind = kind,
      affectedAddress = affectedAddress,
      affectedObjects = affectedObjects,
      sentAddress = sentAddress,
    )
}
