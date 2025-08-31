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
import xyz.mcxross.ksui.generated.ResolveNameServiceAddressQuery
import xyz.mcxross.ksui.generated.ResolveNameServiceNamesQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

/**
 * Defines the API for interacting with the Sui Name Service (SNS).
 *
 * This interface provides methods for resolving `.sui` domain names to addresses and performing
 * reverse lookups to find names associated with an address.
 */
interface Sns {

  /**
   * Resolves a `.sui` domain name to its corresponding Sui address.
   *
   * @param domain The `.sui` domain name to resolve (e.g., "example.sui").
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [ResolveNameServiceAddressQuery.Data] object with the resolved
   *   address.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun resolveNameServiceAddress(
    domain: String
  ): Result<ResolveNameServiceAddressQuery.Data?, SuiError>

  /**
   * Performs a reverse lookup to find all `.sui` domain names associated with a given address.
   *
   * @param address The [AccountAddress] to find the associated domain names for.
   * @param limit An optional integer to specify the maximum number of names to return per page.
   * @param cursor An optional cursor string for pagination.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [ResolveNameServiceNamesQuery.Data] object with a list of names
   *   and a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  suspend fun resolveNameServiceNames(
    address: AccountAddress,
    limit: Int? = null,
    cursor: String? = null,
  ): Result<ResolveNameServiceNamesQuery.Data?, SuiError>
}
