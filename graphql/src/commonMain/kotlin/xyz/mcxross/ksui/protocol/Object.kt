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
package xyz.mcxross.ksui.protocol

import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.GetDynamicFieldObjectQuery
import xyz.mcxross.ksui.generated.GetDynamicFieldsQuery
import xyz.mcxross.ksui.generated.GetObjectQuery
import xyz.mcxross.ksui.generated.GetOwnedObjectsQuery
import xyz.mcxross.ksui.generated.MultiGetObjectsQuery
import xyz.mcxross.ksui.generated.TryGetPastObjectQuery
import xyz.mcxross.ksui.generated.type.DynamicFieldName
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.Result

/** Defines the API for fetching and interacting with on-chain objects in the Sui network. */
interface Object {

  /**
   * Fetches the details of a specific object by its ID.
   *
   * @param id The unique ID of the object to fetch.
   * @param options The options specifying which fields of the object to include in the response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetObjectQuery.Data] object with the object's details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getObject(
    id: String,
    options: ObjectDataOptions = ObjectDataOptions(),
  ): Result<GetObjectQuery.Data?, SuiError>

  /**
   * Fetches a paginated list of objects owned by a specific address.
   *
   * @param owner The [AccountAddress] of the owner.
   * @param limit An optional integer to specify the maximum number of objects to return per page.
   * @param cursor An optional cursor string for pagination.
   * @param options The options specifying which fields of the objects to include in the response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetOwnedObjectsQuery.Data] object with a list of objects and a
   *   pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getOwnedObjects(
    owner: AccountAddress,
    limit: Int? = null,
    cursor: String? = null,
    options: ObjectDataOptions = ObjectDataOptions(),
  ): Result<GetOwnedObjectsQuery.Data?, SuiError>

  /**
   * Fetches the details of multiple objects in a single batch request.
   *
   * @param ids A list of unique IDs of the objects to fetch.
   * @param limit An optional integer to specify the maximum number of objects to return per page.
   * @param cursor An optional cursor string for pagination.
   * @param options The options specifying which fields of the objects to include in the response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [MultiGetObjectsQuery.Data] object with a list of objects.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun multiGetObjects(
    ids: List<String>,
    limit: Int? = null,
    cursor: String? = null,
    options: ObjectDataOptions = ObjectDataOptions(),
  ): Result<MultiGetObjectsQuery.Data?, SuiError>

  /**
   * Fetches a historical version of a specific object.
   *
   * This allows querying for an object's state at a specific point in its history. The query may
   * fail if the requested version has been pruned from the node.
   *
   * @param id The unique ID of the object.
   * @param version The version number of the object to retrieve.
   * @param options The options specifying which fields of the past object to include in the
   *   response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [TryGetPastObjectQuery.Data] object with the past object's
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun tryGetPastObject(
    id: String,
    version: Int? = null,
    options: ObjectDataOptions = ObjectDataOptions(),
  ): Result<TryGetPastObjectQuery.Data?, SuiError>

  /**
   * Fetches an object that is stored as a dynamic field on a parent object.
   *
   * @param parentId The ID of the parent object that owns the dynamic field.
   * @param name The name of the dynamic field, which includes its type and value.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetDynamicFieldObjectQuery.Data] object with the dynamic field's
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getDynamicFieldObject(
    parentId: String,
    name: DynamicFieldName,
  ): Result<GetDynamicFieldObjectQuery.Data?, SuiError>

  /**
   * Fetches a paginated list of all dynamic fields owned by a parent object.
   *
   * @param parentId The ID of the parent object.
   * @param limit An optional integer to specify the maximum number of fields to return per page.
   * @param cursor An optional cursor string for pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetDynamicFieldsQuery.Data] object with a list of dynamic fields
   *   and a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun getDynamicFields(
    parentId: String,
    limit: Int? = null,
    cursor: String? = null,
  ): Result<GetDynamicFieldsQuery.Data?, SuiError>
}
