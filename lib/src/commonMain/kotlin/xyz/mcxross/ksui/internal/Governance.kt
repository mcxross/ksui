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
import xyz.mcxross.ksui.generated.GetCommitteeInfoByEpoch
import xyz.mcxross.ksui.generated.getcommitteeinfobyepoch.Epoch
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getCommitteeInfo(config: SuiConfig, epoch: Long?): Option<Epoch?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request = GetCommitteeInfoByEpoch(GetCommitteeInfoByEpoch.Variables(epoch?.toInt()))
  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.epoch)
}
