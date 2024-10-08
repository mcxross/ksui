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

import xyz.mcxross.ksui.generated.inputs.DynamicFieldName
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.DynamicFieldObject
import xyz.mcxross.ksui.model.DynamicFields
import xyz.mcxross.ksui.model.ObjectDataOptions
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.OwnedObjects
import xyz.mcxross.ksui.model.PastObject

/**
 * Object interface
 *
 * This interface represents the object API
 */
interface Object {

  /**
   * Get an object by ID
   *
   * @param id The ID of the object
   * @param option The options for the object data
   * @return An [Option] of nullable [Object]
   */
  suspend fun getObject(
    id: String,
    option: ObjectDataOptions = ObjectDataOptions(),
  ): Option<xyz.mcxross.ksui.model.Object>

  /**
   * Get owned objects
   *
   * @param owner The owner of the objects
   * @param limit The limit of objects to get
   * @param cursor The cursor to get objects from
   * @param option The options for the object data
   * @return An [Option] of nullable [OwnedObjects]
   */
  suspend fun getOwnedObjects(
    owner: AccountAddress,
    limit: Int? = null,
    cursor: String? = null,
    option: ObjectDataOptions = ObjectDataOptions(),
  ): Option<OwnedObjects>

  /**
   * Try to get a past object
   *
   * @param id The ID of the object
   * @param version The version of the object
   * @return An [Option] of nullable [PastObject]
   */
  suspend fun tryGetPastObject(id: String, version: Int?): Option<PastObject>

  /**
   * Get a dynamic field object
   *
   * @param parentId The parent ID of the object
   * @param name The name of the dynamic field
   * @return An [Option] of nullable [DynamicFieldObject]
   */
  suspend fun getDynamicFieldObject(
    parentId: String,
    name: DynamicFieldName,
  ): Option<DynamicFieldObject>

  /**
   * Get dynamic fields
   *
   * @param parentId The parent ID of the object
   * @param limit The limit of dynamic fields to get
   * @param cursor The cursor to get dynamic fields from
   * @return An [Option] of nullable [DynamicFields]
   */
  suspend fun getDynamicFields(
    parentId: String,
    limit: UInt?,
    cursor: String?,
  ): Option<DynamicFields>
}
