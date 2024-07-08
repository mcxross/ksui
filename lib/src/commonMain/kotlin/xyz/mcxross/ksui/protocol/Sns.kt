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

import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Page
import xyz.mcxross.ksui.model.SuiAddress

/**
 * Sns interface
 *
 * This interface represents the SNS API
 */
interface Sns {

  /**
   * Resolve name service address
   *
   * @param domain The domain to resolve
   * @return An [Option] of nullable [SuiAddress]
   */
  suspend fun resolveNameServiceAddress(domain: String): Option<SuiAddress?>

  /**
   * Resolve name service names
   *
   * @param address The address to resolve
   * @param limit The limit of names to resolve
   * @param cursor The cursor to resolve names from
   * @return An [Option] of nullable [Page]
   */
  suspend fun resolveNameServiceNames(
    address: SuiAddress,
    limit: UInt? = null,
    cursor: String? = null,
  ): Option<Page>
}
