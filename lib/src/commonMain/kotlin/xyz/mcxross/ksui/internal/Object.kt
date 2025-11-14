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
import xyz.mcxross.ksui.generated.GetDynamicFieldObjectQuery
import xyz.mcxross.ksui.generated.GetDynamicFieldsQuery
import xyz.mcxross.ksui.generated.GetObjectQuery
import xyz.mcxross.ksui.generated.GetOwnedObjectsQuery
import xyz.mcxross.ksui.generated.MultiGetObjectsQuery
import xyz.mcxross.ksui.generated.TryGetPastObjectQuery
import xyz.mcxross.ksui.generated.type.DynamicFieldName
import xyz.mcxross.ksui.generated.type.ObjectKey
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getObject(
  config: SuiConfig,
  id: String,
  option: ObjectDataOptions,
): Result<GetObjectQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetObjectQuery(
            id,
            showBcs = Optional.presentIfNotNull(option.showBcs),
            showOwner = Optional.presentIfNotNull(option.showOwner),
            showPreviousTransaction = Optional.presentIfNotNull(option.showOwner),
            showContent = Optional.presentIfNotNull(option.showContent),
            showDisplay = Optional.presentIfNotNull(option.showDisplay),
            showType = Optional.presentIfNotNull(option.showType),
            showStorageRebate = Optional.presentIfNotNull(option.showStorageRebate),
          )
        )
    }
    .toResult()

internal suspend fun getOwnedObjects(
  config: SuiConfig,
  owner: AccountAddress,
  limit: Int?,
  cursor: String?,
  option: ObjectDataOptions,
): Result<GetOwnedObjectsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetOwnedObjectsQuery(
            owner.toString(),
            limit = Optional.presentIfNotNull(limit),
            cursor = Optional.presentIfNotNull(cursor),
            showBcs = Optional.presentIfNotNull(option.showBcs),
            showOwner = Optional.presentIfNotNull(option.showOwner),
            showPreviousTransaction = Optional.presentIfNotNull(option.showOwner),
            showContent = Optional.presentIfNotNull(option.showContent),
            showDisplay = Optional.presentIfNotNull(option.showDisplay),
            showType = Optional.presentIfNotNull(option.showType),
            showStorageRebate = Optional.presentIfNotNull(option.showStorageRebate),
          )
        )
    }
    .toResult()

suspend fun multiGetObjects(
  config: SuiConfig,
  ids: List<String>,
  limit: Int?,
  cursor: String?,
  options: ObjectDataOptions,
): Result<MultiGetObjectsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          MultiGetObjectsQuery(
            ids.map { ObjectKey(it) },
            showBcs = Optional.presentIfNotNull(options.showBcs),
            showOwner = Optional.presentIfNotNull(options.showOwner),
            showPreviousTransaction = Optional.presentIfNotNull(options.showOwner),
            showContent = Optional.presentIfNotNull(options.showContent),
            showDisplay = Optional.presentIfNotNull(options.showDisplay),
            showType = Optional.presentIfNotNull(options.showType),
            showStorageRebate = Optional.presentIfNotNull(options.showStorageRebate),
          )
        )
    }
    .toResult()

suspend fun tryGetPastObject(
  config: SuiConfig,
  id: String,
  version: Int?,
  option: ObjectDataOptions,
): Result<TryGetPastObjectQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          TryGetPastObjectQuery(
            id,
            version = Optional.presentIfNotNull(version),
            showBcs = Optional.presentIfNotNull(option.showBcs),
            showOwner = Optional.presentIfNotNull(option.showOwner),
            showPreviousTransaction = Optional.presentIfNotNull(option.showOwner),
            showContent = Optional.presentIfNotNull(option.showContent),
            showDisplay = Optional.presentIfNotNull(option.showDisplay),
            showType = Optional.presentIfNotNull(option.showType),
            showStorageRebate = Optional.presentIfNotNull(option.showStorageRebate),
          )
        )
    }
    .toResult()

suspend fun getDynamicFieldObject(
  config: SuiConfig,
  parentId: String,
  name: DynamicFieldName,
): Result<GetDynamicFieldObjectQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetDynamicFieldObjectQuery(parentId, name)) }
    .toResult()

suspend fun getDynamicFields(
  config: SuiConfig,
  parentId: String,
  first: Int?,
  cursor: String?,
): Result<GetDynamicFieldsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetDynamicFieldsQuery(
            parentId,
            first = Optional.presentIfNotNull(first),
            cursor = Optional.presentIfNotNull(cursor),
          )
        )
    }
    .toResult()
