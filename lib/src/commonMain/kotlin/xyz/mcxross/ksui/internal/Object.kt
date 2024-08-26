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

import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.generated.GetDynamicFieldObject
import xyz.mcxross.ksui.generated.GetDynamicFields
import xyz.mcxross.ksui.generated.GetObject
import xyz.mcxross.ksui.generated.GetOwnedObjects
import xyz.mcxross.ksui.generated.TryGetPastObject
import xyz.mcxross.ksui.generated.inputs.DynamicFieldName
import xyz.mcxross.ksui.model.DynamicFieldObject
import xyz.mcxross.ksui.model.DynamicFields
import xyz.mcxross.ksui.model.Object
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.OwnedObjects
import xyz.mcxross.ksui.model.PastObject
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getObject(
  config: SuiConfig,
  id: String,
  option: ObjectDataOptions,
): Option<Object> {

  val query =
    GetObject(
      GetObject.Variables(
        id = id,
        showBcs = option.showBcs,
        showOwner = option.showOwner,
        showPreviousTransaction = option.showPreviousTransaction,
        showContent = option.showContent,
        showDisplay = option.showDisplay,
        showType = option.showType,
        showStorageRebate = option.showStorageRebate,
      )
    )

  val response = getGraphqlClient(config).execute(query)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal suspend fun getOwnedObjects(
  config: SuiConfig,
  owner: SuiAddress,
  limit: Int?,
  cursor: String?,
  option: ObjectDataOptions,
): Option<OwnedObjects> {
  val response =
    getGraphqlClient(config)
      .execute(
        GetOwnedObjects(
          GetOwnedObjects.Variables(
            owner = owner.toString(),
            limit = limit,
            cursor = cursor,
            showBcs = option.showBcs,
            showContent = option.showContent,
            showDisplay = option.showDisplay,
            showOwner = option.showOwner,
            showPreviousTransaction = option.showPreviousTransaction,
            showStorageRebate = option.showStorageRebate,
          )
        )
      )

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

suspend fun tryGetPastObject(config: SuiConfig, id: String, version: Int?): Option<PastObject> {

  val query = TryGetPastObject(TryGetPastObject.Variables(id, version))

  val response = getGraphqlClient(config).execute(query)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

suspend fun getDynamicFieldObject(
  config: SuiConfig,
  parentId: String,
  name: DynamicFieldName,
): Option<DynamicFieldObject> {
  val query =
    GetDynamicFieldObject(GetDynamicFieldObject.Variables(parentId = parentId, name = name))

  val response = getGraphqlClient(config).execute(query)

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

suspend fun getDynamicFields(
  config: SuiConfig,
  parentId: String,
  limit: UInt?,
  cursor: String?,
): Option<DynamicFields> {

  val response =
    getGraphqlClient(config)
      .execute(
        GetDynamicFields(
          GetDynamicFields.Variables(parentId = parentId, first = limit?.toInt(), cursor = cursor)
        )
      )

  if (!response.errors.isNullOrEmpty()) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}
