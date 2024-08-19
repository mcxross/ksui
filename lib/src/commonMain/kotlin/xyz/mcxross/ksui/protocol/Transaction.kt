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
import xyz.mcxross.ksui.model.TransactionBlocks
import xyz.mcxross.ksui.model.TransactionBlocksOptions

/**
 * Transaction interface
 *
 * This interface represents the transaction API
 */
interface Transaction {

  /**
   * Get the total transaction blocks
   *
   * @return An [Option] of nullable [Long]
   */
  suspend fun getTotalTransactionBlocks(): Option<Long?>

  /**
   * Query transaction blocks for the specified criteria
   *
   * @param txnBlocksOptions The options to use
   * @return An [Option] of nullable [TransactionBlocks]
   */
  suspend fun queryTransactionBlocks(
    txnBlocksOptions: TransactionBlocksOptions = TransactionBlocksOptions()
  ): Option<TransactionBlocks>
}
