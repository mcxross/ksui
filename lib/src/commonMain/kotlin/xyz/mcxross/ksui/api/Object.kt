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

import xyz.mcxross.ksui.generated.inputs.DynamicFieldName
import xyz.mcxross.ksui.internal.getDynamicFieldObject
import xyz.mcxross.ksui.internal.getDynamicFields
import xyz.mcxross.ksui.internal.getOwnedObjects
import xyz.mcxross.ksui.model.DynamicFieldObject
import xyz.mcxross.ksui.model.DynamicFields
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.OwnedObjects
import xyz.mcxross.ksui.model.PastObject
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Object

/**
 * Object API implementation
 *
 * This namespace contains all the functions related to objects
 *
 * @property config The SuiConfig to use
 */
class Object(val config: SuiConfig) : Object {

  /**
   * Get an object by ID
   *
   * @param id The ID of the object to get
   * @param option The options to use when getting the object
   * @return An [Option] of nullable [xyz.mcxross.ksui.model.Object]
   */
  override suspend fun getObject(
    id: String,
    option: ObjectDataOptions,
  ): Option<xyz.mcxross.ksui.model.Object> = xyz.mcxross.ksui.internal.getObject(config, id, option)

  /**
   * Get owned objects
   *
   * @param owner The owner of the objects to get
   * @param limit The limit of objects to get
   * @param cursor The cursor to get objects from
   * @param option The options to use when getting the objects
   * @return An [Option] of nullable [OwnedObjects]
   */
  override suspend fun getOwnedObjects(
    owner: SuiAddress,
    limit: Int?,
    cursor: String?,
    option: ObjectDataOptions,
  ): Option<OwnedObjects> = getOwnedObjects(config, owner, limit, cursor, option)

  /**
   * Try to get a past object
   *
   * @param id The ID of the object
   * @param version The version of the object
   * @return a `[PastObject]`
   */
  override suspend fun tryGetPastObject(id: String, version: Int?): Option<PastObject> =
    xyz.mcxross.ksui.internal.tryGetPastObject(config, id, version)

  /**
   * Get a dynamic field object
   *
   * @param parentId The parent ID of the object to get
   * @param name The name of the dynamic field to get
   * @return An [Option] of nullable [DynamicFieldObject]
   */
  override suspend fun getDynamicFieldObject(
    parentId: String,
    name: DynamicFieldName,
  ): Option<DynamicFieldObject> = getDynamicFieldObject(config, parentId, name)

  /**
   * Get dynamic fields
   *
   * @param parentId The parent ID of the fields to get
   * @param limit The limit of fields to get
   * @param cursor The cursor to get fields from
   * @return An [Option] of nullable [DynamicFields]
   */
  override suspend fun getDynamicFields(
    parentId: String,
    limit: UInt?,
    cursor: String?,
  ): Option<DynamicFields> = getDynamicFields(config, parentId, limit, cursor)
}
