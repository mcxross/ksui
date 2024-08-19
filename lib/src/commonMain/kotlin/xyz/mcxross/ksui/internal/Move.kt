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
import xyz.mcxross.ksui.extension.IdParts
import xyz.mcxross.ksui.generated.GetMoveFunctionArgTypes
import xyz.mcxross.ksui.model.MoveFunctionArgTypes
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig

suspend fun getMoveFunctionArgTypes(
  config: SuiConfig,
  idParts: IdParts,
): Option<MoveFunctionArgTypes> {
  val query =
    GetMoveFunctionArgTypes(
      GetMoveFunctionArgTypes.Variables(
        packageId = idParts.packageId,
        module = idParts.module,
        function = idParts.function,
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
