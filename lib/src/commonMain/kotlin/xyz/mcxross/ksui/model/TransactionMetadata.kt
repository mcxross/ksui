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
import xyz.mcxross.ksui.generated.type.TransactionMetadata

data class TransactionMetaData(
  val sender: String? = null,
  val gasPrice: Long? = null,
  val gasObjects: List<ObjectRef>? = emptyList(),
  val gasBudget: Long?,
  val gasSponsor: AccountAddress,
) {
  fun toGenerated(): TransactionMetadata {
    return TransactionMetadata(
      Optional.presentIfNotNull(sender),
      Optional.presentIfNotNull(gasPrice),
      Optional.presentIfNotNull(gasObjects?.map { it.toGenerated() }),
      Optional.presentIfNotNull(gasBudget),
      Optional.presentIfNotNull(gasSponsor.toString()),
    )
  }
}
