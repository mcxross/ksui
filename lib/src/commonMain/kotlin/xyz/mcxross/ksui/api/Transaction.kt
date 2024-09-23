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

import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.generated.DryRunTransactionBlock
import xyz.mcxross.ksui.generated.ExecuteTransactionBlock
import xyz.mcxross.ksui.internal.dryRunTransactionBlock
import xyz.mcxross.ksui.internal.executeTransactionBlock
import xyz.mcxross.ksui.internal.getTotalTransactionBlocks
import xyz.mcxross.ksui.internal.signAndSubmitTransaction
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResult
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionBlocks
import xyz.mcxross.ksui.protocol.Transaction
import xyz.mcxross.ksui.ptb.ProgrammableTransaction

/**
 * Transaction API implementation
 *
 * This namespace contains all the functions related to transactions
 *
 * @property config The SuiConfig to use
 */
class Transaction(val config: SuiConfig) : Transaction {

  /**
   * Execute a transaction block
   *
   * This function will execute a transaction block with the given transaction bytes and signatures.
   *
   * @param txnBytes The transaction bytes
   * @param signatures The signatures
   * @param option The options to use for response
   * @return An [Option] of nullable [ExecuteTransactionBlockResult]
   */
  override suspend fun executeTransactionBlock(
    txnBytes: String,
    signatures: List<String>,
    option: ExecuteTransactionBlockResponseOptions,
  ): Option.Some<ExecuteTransactionBlockResult> =
    executeTransactionBlock(config, txnBytes, signatures, option)

  /**
   * Dry run a transaction block
   *
   * This function will dry run a transaction block with the given transaction bytes.
   *
   * @param txnBytes The transaction bytes
   * @param option The options to use for response
   * @return An [Option] of nullable [DryRunTransactionBlock.Result]
   */
  override suspend fun dryRunTransactionBlock(
    txnBytes: String,
    option: ExecuteTransactionBlockResponseOptions,
  ): Option.Some<DryRunTransactionBlock.Result?> = dryRunTransactionBlock(config, txnBytes, option)

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
    txnBlocksOptions: TransactionBlockResponseOptions
  ): Option<TransactionBlocks> =
    xyz.mcxross.ksui.internal.queryTransactionBlocks(config, txnBlocksOptions)

  /**
   * Sign a transaction
   *
   * This function will sign a transaction with the given message and signer.
   *
   * @param message The message
   * @param signer The signer
   * @return The signed transaction
   */
  override fun signTransaction(message: ByteArray, signer: Account): ByteArray =
    xyz.mcxross.ksui.internal.signTransaction(message, signer)

  /**
   * Sign and execute a transaction block
   *
   * This function will sign and execute a transaction block with the given programmable transaction
   * and signer.
   *
   * @param ptb The programmable transaction
   * @param signer The signer
   * @param gasBudget The gas budget
   * @return An [Option] of nullable [ExecuteTransactionBlockResult]
   */
  override suspend fun signAndExecuteTransactionBlock(
    signer: Account,
    ptb: ProgrammableTransaction,
    gasBudget: ULong,
  ): Option.Some<ExecuteTransactionBlockResult> =
    signAndSubmitTransaction(config, signer, ptb, gasBudget)
}
