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

import xyz.mcxross.ksui.internal.getTotalTransactionBlocks
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.TransactionBlocks
import xyz.mcxross.ksui.model.TransactionBlocksOptions
import xyz.mcxross.ksui.protocol.Transaction

/**
 * Transaction API implementation
 *
 * This namespace contains all the functions related to transactions
 *
 * @property config The SuiConfig to use
 */
class Transaction(val config: SuiConfig) : Transaction {

  /**
   * Get the total transaction blocks
   *
   * @return An [Option] of nullable [Long]
   */
  override suspend fun getTotalTransactionBlocks(): Option<Long?> =
    getTotalTransactionBlocks(config)

  /**
   * Query transaction blocks for the specified criteria
   *
   * @param txnBlocksOptions The options to use
   * @return An [Option] of nullable [TransactionBlocks]
   */
  override suspend fun queryTransactionBlocks(
    txnBlocksOptions: TransactionBlocksOptions
  ): Option<TransactionBlocks> =
    xyz.mcxross.ksui.internal.queryTransactionBlocks(config, txnBlocksOptions)
}
