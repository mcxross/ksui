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

import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.extension.asIdParts
import xyz.mcxross.ksui.generated.GetMoveFunctionArgTypesQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveFunctionQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveModuleQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveModulesByPackageQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveStructQuery
import xyz.mcxross.ksui.generated.GetTypeLayoutQuery
import xyz.mcxross.ksui.generated.PaginateMoveModuleListsQuery
import xyz.mcxross.ksui.internal.getMoveFunctionArgTypes
import xyz.mcxross.ksui.internal.getNormalizedMoveFunction
import xyz.mcxross.ksui.internal.getNormalizedMoveModule
import xyz.mcxross.ksui.internal.getNormalizedMoveModulesByPackage
import xyz.mcxross.ksui.internal.getNormalizedMoveStruct
import xyz.mcxross.ksui.internal.getTypeLayout
import xyz.mcxross.ksui.internal.paginateMoveModuleLists
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Move

/**
 * The concrete implementation of the [Move] interface.
 *
 * @param config The [SuiConfig] object specifying the RPC endpoint and connection settings.
 */
class Move(val config: SuiConfig) : Move {

  /**
   * Fetches the argument types for a specific Move function.
   *
   * This implementation parses the input `id` string (e.g., "0x2::sui::transfer_object") into its
   * constituent parts before making the query.
   *
   * @param id The identifier of the Move function in the format `package::module::function`.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetMoveFunctionArgTypesQuery.Data] object with the argument
   *   types.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getMoveFunctionArgTypes(
    id: String
  ): Result<GetMoveFunctionArgTypesQuery.Data?, SuiError> {
    val parts = id.asIdParts()
    return getMoveFunctionArgTypes(config, parts.first, parts.second, parts.third)
  }

  /**
   * Fetches the normalized (desugared and with types expanded) representation of a Move function.
   *
   * @param id The identifier of the Move function in the format `package::module::function`.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetNormalizedMoveFunctionQuery.Data] object with the normalized
   *   function details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getNormalizedMoveFunction(
    id: String
  ): Result<GetNormalizedMoveFunctionQuery.Data?, SuiError> {
    val parts = id.asIdParts()
    return getNormalizedMoveFunction(config, parts.first, parts.second, parts.third)
  }

  /**
   * Fetches the normalized representation of a specific module within a package.
   *
   * @param packageId The ID of the package containing the module.
   * @param module The name of the module.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetNormalizedMoveModuleQuery.Data] object with the normalized
   *   module details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getNormalizedMoveModule(
    packageId: String,
    module: String,
  ): Result<GetNormalizedMoveModuleQuery.Data?, SuiError> =
    getNormalizedMoveModule(config, packageId, module)

  /**
   * Fetches a paginated list of all normalized modules within a given package.
   *
   * @param packageId The ID of the package to query modules from.
   * @param cursor An optional cursor string for pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetNormalizedMoveModulesByPackageQuery.Data] object with a list
   *   of modules and a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getNormalizedMoveModulesByPackage(
    packageId: String,
    cursor: String?,
  ): Result<GetNormalizedMoveModulesByPackageQuery.Data?, SuiError> =
    getNormalizedMoveModulesByPackage(config, packageId, cursor)

  /**
   * Fetches the normalized representation of a Move struct.
   *
   * @param id The identifier of the Move struct in the format `package::module::struct`.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetNormalizedMoveStructQuery.Data] object with the normalized
   *   struct details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getNormalizedMoveStruct(
    id: String
  ): Result<GetNormalizedMoveStructQuery.Data?, SuiError> {
    val parts = id.asIdParts()
    return getNormalizedMoveStruct(config, parts.first, parts.second, parts.third)
  }

  /**
   * Resolves the on-chain memory layout for a specified Move type.
   *
   * This is useful for serialization and deserialization.
   *
   * @param type The string representation of the Move type to resolve.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetTypeLayoutQuery.Data] object with the type layout details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getTypeLayout(type: String): Result<GetTypeLayoutQuery.Data?, SuiError> =
    getTypeLayout(config, type)

  /**
   * Fetches paginated lists of components within a Move module.
   *
   * This function allows for independent pagination of each component list (friends, structs,
   * functions, enums) by using boolean flags to selectively include them in the query.
   *
   * @param packageId The ID of the package containing the module.
   * @param module The name of the module.
   * @param hasMoreFriends A flag to control whether to fetch the `friends` list. Set to `true` to
   *   include it in the response.
   * @param hasMoreStructs A flag to control whether to fetch the `structs` list. Set to `true` to
   *   include it in the response.
   * @param hasMoreFunctions A flag to control whether to fetch the `functions` list. Set to `true`
   *   to include it in the response.
   * @param hasMoreEnums A flag to control whether to fetch the `enums` list. Set to `true` to
   *   include it in the response.
   * @param afterFriends An optional cursor to paginate through the `friends` list.
   * @param afterStructs An optional cursor to paginate through the `structs` list.
   * @param afterFunctions An optional cursor to paginate through the `functions` list.
   * @param afterEnums An optional cursor to paginate through the `enums` list.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [PaginateMoveModuleListsQuery.Data] object with the requested
   *   paginated lists of the module's components.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun paginateMoveModuleLists(
    packageId: String,
    module: String,
    hasMoreFriends: Boolean,
    hasMoreStructs: Boolean,
    hasMoreFunctions: Boolean,
    hasMoreEnums: Boolean,
    afterFriends: String?,
    afterStructs: String?,
    afterFunctions: String?,
    afterEnums: String?,
  ): Result<PaginateMoveModuleListsQuery.Data?, SuiError> =
    paginateMoveModuleLists(
      config,
      packageId,
      module,
      hasMoreFriends,
      hasMoreStructs,
      hasMoreFunctions,
      hasMoreEnums,
      afterFriends,
      afterStructs,
      afterFunctions,
      afterEnums,
    )
}
