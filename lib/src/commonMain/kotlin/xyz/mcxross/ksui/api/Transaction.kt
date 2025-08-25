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
import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.generated.DevInspectTransactionBlockQuery
import xyz.mcxross.ksui.generated.ExecuteTransactionBlockMutation
import xyz.mcxross.ksui.generated.GetTransactionBlockQuery
import xyz.mcxross.ksui.generated.PaginateTransactionBlockListsQuery
import xyz.mcxross.ksui.generated.QueryTransactionBlocksQuery
import xyz.mcxross.ksui.internal.devInspectTransactionBlock
import xyz.mcxross.ksui.internal.dryRunTransactionBlock
import xyz.mcxross.ksui.internal.executeTransactionBlock
import xyz.mcxross.ksui.internal.getTotalTransactionBlocks
import xyz.mcxross.ksui.internal.getTransactionBlock
import xyz.mcxross.ksui.internal.paginateTransactionBlockLists
import xyz.mcxross.ksui.internal.queryTransactionBlocks
import xyz.mcxross.ksui.internal.signAndSubmitTransaction
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.TransactionBlockFilter
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionMetaData
import xyz.mcxross.ksui.protocol.Transaction
import xyz.mcxross.ksui.ptb.ProgrammableTransaction

/**
 * The concrete implementation of the [Transaction] interface.
 *
 * This class provides all the functions related to building, executing, and querying transactions.
 *
 * @property config The [SuiConfig] object specifying the RPC endpoint and connection settings.
 */
class Transaction(val config: SuiConfig) : Transaction {

  /**
   * Creates a cryptographic signature for a given message using a signer's private key.
   *
   * This is a local, synchronous operation that does not require a network connection. It is a
   * fundamental building block for creating valid transactions.
   *
   * @param message The raw message bytes to be signed (typically serialized transaction data).
   * @param signer The [Account] containing the private key to sign with.
   * @return A [ByteArray] representing the resulting signature.
   */
  override suspend fun signTransaction(message: ByteArray, signer: Account): ByteArray =
    xyz.mcxross.ksui.internal.signTransaction(message, signer)

  /**
   * Runs a special developer-focused inspection of a transaction block.
   *
   * This is a powerful debugging tool that can reveal detailed effects, potential errors, and
   * execution results without broadcasting the transaction or requiring a valid signature. Unlike a
   * standard dry run, this endpoint requires specifying transaction metadata like the sender and
   * gas information directly.
   *
   * @param txBytes The base64-encoded, BCS-serialized transaction data.
   * @param txMetaData A [TransactionMetaData] object specifying the sender address, gas object, gas
   *   price, and other details for the inspection.
   * @param options The [ExecuteTransactionBlockResponseOptions] to tailor which details are
   *   returned in the response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [DevInspectTransactionBlockQuery.Data] object with the detailed
   *   inspection results.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun devInspectTransactionBlock(
    txBytes: String,
    txMetaData: TransactionMetaData,
    options: ExecuteTransactionBlockResponseOptions,
  ): Result<DevInspectTransactionBlockQuery.Data?, SuiError> =
    devInspectTransactionBlock(config, txBytes, txMetaData, options)

  /**
   * Simulates the execution of a transaction block without committing it to the network.
   *
   * This is useful for estimating gas costs, verifying outcomes, and debugging potential errors
   * before submission.
   *
   * @param txBytes The base64-encoded, BCS-serialized transaction data.
   * @param option The response options to tailor which details of the dry run are returned.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [DryRunTransactionBlockQuery.Data] object with the results of the
   *   simulation, such as the gas summary and effects.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun dryRunTransactionBlock(
    txBytes: String,
    option: ExecuteTransactionBlockResponseOptions,
  ) = dryRunTransactionBlock(config, txBytes, option)

  /**
   * Submits a pre-signed transaction block to the Sui network for execution.
   *
   * @param txBytes The base64-encoded, BCS-serialized transaction data.
   * @param signatures A list of base64-encoded signatures corresponding to the signers.
   * @param option The response options to specify which details of the executed transaction to
   *   return (e.g., effects, object changes).
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [ExecuteTransactionBlockMutation.Data] object with the
   *   transaction response.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun executeTransactionBlock(
    txBytes: String,
    signatures: List<String>,
    option: ExecuteTransactionBlockResponseOptions,
  ) = executeTransactionBlock(config, txBytes, signatures, option)

  /**
   * A convenience method that signs and executes a transaction block in a single step.
   *
   * @param signer The [Account] to sign the transaction with.
   * @param ptb The [ProgrammableTransaction] block to be executed.
   * @param gasBudget The maximum gas budget for the transaction.
   */
  override suspend fun signAndExecuteTransactionBlock(
    signer: Account,
    ptb: ProgrammableTransaction,
    gasBudget: ULong,
    options: ExecuteTransactionBlockResponseOptions,
  ): Result<ExecuteTransactionBlockMutation.Data?, SuiError> =
    signAndSubmitTransaction(config, signer, ptb, gasBudget, options)

  /**
   * Fetches the details of a specific transaction block by its digest.
   *
   * @param digest The base58-encoded digest of the transaction block.
   * @param options The [TransactionBlockResponseOptions] specifying which details of the
   *   transaction to include in the response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [GetTransactionBlockQuery.Data] object with the transaction's
   *   details.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun getTransactionBlock(
    digest: String,
    options: TransactionBlockResponseOptions,
  ): Result<GetTransactionBlockQuery.Data?, SuiError> = getTransactionBlock(config, digest, options)

  /**
   * Fetches a paginated list of transaction blocks matching a specified filter.
   *
   * @param filter A [TransactionBlockFilter] to narrow down the search criteria (e.g., by sender,
   *   recipient, or object ID).
   * @param options The [TransactionBlockResponseOptions] specifying which details of the matching
   *   transactions to include in the response.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [QueryTransactionBlocksQuery.Data] object with a list of
   *   transaction blocks and a pagination cursor.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun queryTransactionBlocks(
    filter: TransactionBlockFilter,
    options: TransactionBlockResponseOptions,
  ): Result<QueryTransactionBlocksQuery.Data?, SuiError> =
    queryTransactionBlocks(config, filter, options)

  /**
   * Fetches paginated lists of components within a specific transaction block.
   *
   * This allows for independent pagination of events, balance changes, and object changes
   * associated with a single transaction.
   *
   * @param digest The digest of the transaction block to inspect.
   * @param hasMoreEvents A flag to control whether to fetch the `events` list.
   * @param hasMoreBalanceChanges A flag to control whether to fetch the `balanceChanges` list.
   * @param hasMoreObjectChanges A flag to control whether to fetch the `objectChanges` list.
   * @param afterEvents An optional cursor for paginating through events.
   * @param afterBalanceChanges An optional cursor for paginating through balance changes.
   * @param afterObjectChanges An optional cursor for paginating through object changes.
   * @return A [Result] which is either:
   * - `Ok`: Containing a nullable [PaginateTransactionBlockListsQuery.Data] object with the
   *   requested component lists.
   * - `Err`: Containing a [SuiError] object with a list of [GraphQLError]s.
   */
  override suspend fun paginateTransactionBlockLists(
    digest: String,
    hasMoreEvents: Boolean,
    hasMoreBalanceChanges: Boolean,
    hasMoreObjectChanges: Boolean,
    afterEvents: String?,
    afterBalanceChanges: String?,
    afterObjectChanges: String?,
  ): Result<PaginateTransactionBlockListsQuery.Data?, SuiError> =
    paginateTransactionBlockLists(
      config,
      digest,
      hasMoreEvents,
      hasMoreBalanceChanges,
      hasMoreObjectChanges,
      afterEvents,
      afterBalanceChanges,
      afterObjectChanges,
    )

  /**
   * Fetches the total number of transaction blocks processed by the network.
   *
   * @return An [Option] of nullable [Long]
   */
  override suspend fun getTotalTransactionBlocks() = getTotalTransactionBlocks(config)

  override suspend fun waitForTransaction(
    digest: String,
    options: TransactionBlockResponseOptions,
    timeout: Long,
    pollInterval: Long,
  ): Result<GetTransactionBlockQuery.Data?, SuiError> =
    xyz.mcxross.ksui.internal.waitForTransaction(config, digest, options, timeout, pollInterval)
}
