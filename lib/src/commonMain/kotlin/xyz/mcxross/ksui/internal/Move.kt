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
import xyz.mcxross.ksui.generated.GetMoveFunctionArgTypesQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveFunctionQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveModuleQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveModulesByPackageQuery
import xyz.mcxross.ksui.generated.GetNormalizedMoveStructQuery
import xyz.mcxross.ksui.generated.GetTypeLayoutQuery
import xyz.mcxross.ksui.generated.PaginateMoveModuleListsQuery
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getMoveFunctionArgTypes(
  config: SuiConfig,
  packageId: String,
  module: String,
  function: String,
): Result<GetMoveFunctionArgTypesQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetMoveFunctionArgTypesQuery(packageId, module, function)) }.toResult()

internal suspend fun getNormalizedMoveFunction(
  config: SuiConfig,
  packageId: Any,
  module: String,
  function: String,
): Result<GetNormalizedMoveFunctionQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetNormalizedMoveFunctionQuery(packageId, module, function)) }
    .toResult()

internal suspend fun getNormalizedMoveModule(
  config: SuiConfig,
  packageId: Any,
  module: String,
): Result<GetNormalizedMoveModuleQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetNormalizedMoveModuleQuery(packageId, module)) }.toResult()

internal suspend fun getNormalizedMoveModulesByPackage(
  config: SuiConfig,
  packageId: String,
  cursor: String?,
): Result<GetNormalizedMoveModulesByPackageQuery.Data?, SuiError> =
  handleQuery {
    getGraphqlClient(config).query(
        GetNormalizedMoveModulesByPackageQuery(packageId, Optional.presentIfNotNull(cursor))
      )
    }
    .toResult()

internal suspend fun getNormalizedMoveStruct(
  config: SuiConfig,
  packageId: Any,
  module: String,
  struct: String,
): Result<GetNormalizedMoveStructQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetNormalizedMoveStructQuery(packageId, module, struct)) }.toResult()

internal suspend fun getTypeLayout(
  config: SuiConfig,
  type: String,
): Result<GetTypeLayoutQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetTypeLayoutQuery(type)) }.toResult()

internal suspend fun paginateMoveModuleLists(
  config: SuiConfig,
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
  handleQuery {
    getGraphqlClient(config).query(
        PaginateMoveModuleListsQuery(
          packageId,
          module,
          hasMoreFriends,
          hasMoreStructs,
          hasMoreFunctions,
          hasMoreEnums,
          afterFriends = Optional.presentIfNotNull(afterFriends),
          afterStructs = Optional.presentIfNotNull(afterStructs),
          afterFunctions = Optional.presentIfNotNull(afterFunctions),
          afterEnums = Optional.presentIfNotNull(afterEnums),
        )
      )
    }
    .toResult()
