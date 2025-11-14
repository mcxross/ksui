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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.exception.GraphQLError
import xyz.mcxross.ksui.exception.SuiError
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.generated.DevInspectTransactionBlockQuery
import xyz.mcxross.ksui.generated.DryRunTransactionBlockQuery
import xyz.mcxross.ksui.generated.ExecuteTransactionBlockMutation
import xyz.mcxross.ksui.generated.GetTotalTransactionBlocksQuery
import xyz.mcxross.ksui.generated.GetTransactionBlockQuery
import xyz.mcxross.ksui.generated.PaginateTransactionBlockListsQuery
import xyz.mcxross.ksui.generated.QueryTransactionBlocksQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.Intent
import xyz.mcxross.ksui.model.IntentMessage
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.TransactionBlockFilter
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.model.TransactionMetaData
import xyz.mcxross.ksui.model.content
import xyz.mcxross.ksui.model.with
import xyz.mcxross.ksui.ptb.ProgrammableTransaction

internal suspend fun devInspectTransactionBlock(
  config: SuiConfig,
  txBytes: String,
  txMetaData: TransactionMetaData,
  options: ExecuteTransactionBlockResponseOptions,
): Result<DevInspectTransactionBlockQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          DevInspectTransactionBlockQuery(
            txBytes,
            showBalanceChanges = Optional.presentIfNotNull(options.showBalanceChanges),
            showEffects = Optional.presentIfNotNull(options.showEffects),
            showRawEffects = Optional.presentIfNotNull(options.showRawEffects),
            showEvents = Optional.presentIfNotNull(options.showEvents),
            showObjectChanges = Optional.presentIfNotNull(options.showObjectChanges),
          )
        )
    }
    .toResult()

internal suspend fun dryRunTransactionBlock(
  config: SuiConfig,
  txBytes: String,
  options: ExecuteTransactionBlockResponseOptions,
): Result<DryRunTransactionBlockQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          DryRunTransactionBlockQuery(
            txBytes,
            showObjectChanges = Optional.presentIfNotNull(options.showBalanceChanges),
            showEffects = Optional.presentIfNotNull(options.showEffects),
            showRawEffects = Optional.presentIfNotNull(options.showRawEffects),
            showEvents = Optional.presentIfNotNull(options.showEvents),
            showBalanceChanges = Optional.presentIfNotNull(options.showObjectChanges),
          )
        )
    }
    .toResult()

internal suspend fun executeTransactionBlock(
  config: SuiConfig,
  txBytes: String,
  signatures: List<String>,
  options: ExecuteTransactionBlockResponseOptions,
): Result<ExecuteTransactionBlockMutation.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .mutation(
          ExecuteTransactionBlockMutation(
            txBytes,
            signatures,
            showBalanceChanges = Optional.presentIfNotNull(options.showBalanceChanges),
            showEffects = Optional.presentIfNotNull(options.showEffects),
            showRawEffects = Optional.presentIfNotNull(options.showRawEffects),
            showEvents = Optional.presentIfNotNull(options.showEvents),
            showInput = Optional.presentIfNotNull(options.showInput),
            showObjectChanges = Optional.presentIfNotNull(options.showObjectChanges),
            showRawInput = Optional.presentIfNotNull(options.showRawInput),
          )
        )
    }
    .toResult()

internal suspend fun getTransactionBlock(
  config: SuiConfig,
  digest: String,
  options: TransactionBlockResponseOptions,
): Result<GetTransactionBlockQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          GetTransactionBlockQuery(
            digest,
            showBalanceChanges = Optional.presentIfNotNull(options.showBalanceChanges),
            showEffects = Optional.presentIfNotNull(options.showEffects),
            showRawEffects = Optional.presentIfNotNull(options.showRawEffects),
            showEvents = Optional.presentIfNotNull(options.showEvents),
            showInput = Optional.presentIfNotNull(options.showInput),
            showObjectChanges = Optional.presentIfNotNull(options.showObjectChanges),
            showRawInput = Optional.presentIfNotNull(options.showRawInput),
          )
        )
    }
    .toResult()

internal suspend fun getTotalTransactionBlocks(
  config: SuiConfig
): Result<GetTotalTransactionBlocksQuery.Data?, SuiError> =
  handleQuery { getGraphqlClient(config).query(GetTotalTransactionBlocksQuery()) }.toResult()

internal suspend fun queryTransactionBlocks(
  config: SuiConfig,
  filter: TransactionBlockFilter,
  options: TransactionBlockResponseOptions,
): Result<QueryTransactionBlocksQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          QueryTransactionBlocksQuery(
            first = Optional.presentIfNotNull(options.first),
            last = Optional.presentIfNotNull(options.last),
            before = Optional.presentIfNotNull(options.before),
            after = Optional.presentIfNotNull(options.after),
            showBalanceChanges = Optional.presentIfNotNull(options.showBalanceChanges),
            showEffects = Optional.presentIfNotNull(options.showEffects),
            showRawEffects = Optional.presentIfNotNull(options.showRawEffects),
            showEvents = Optional.presentIfNotNull(options.showEvents),
            showInput = Optional.presentIfNotNull(options.showInput),
            showObjectChanges = Optional.presentIfNotNull(options.showObjectChanges),
            showRawInput = Optional.presentIfNotNull(options.showRawInput),
            // filter = Optional.presentIfNotNull(filter.toGenerated()),
          )
        )
    }
    .toResult()

internal suspend fun paginateTransactionBlockLists(
  config: SuiConfig,
  digest: String,
  hasMoreEvents: Boolean,
  hasMoreBalanceChanges: Boolean,
  hasMoreObjectChanges: Boolean,
  afterEvents: String?,
  afterBalanceChanges: String?,
  afterObjectChanges: String?,
): Result<PaginateTransactionBlockListsQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(
          PaginateTransactionBlockListsQuery(
            digest,
            hasMoreEvents,
            hasMoreBalanceChanges,
            hasMoreObjectChanges,
            afterEvents = Optional.presentIfNotNull(afterEvents),
            afterBalanceChanges = Optional.presentIfNotNull(afterBalanceChanges),
            afterObjectChanges = Optional.presentIfNotNull(afterObjectChanges),
          )
        )
    }
    .toResult()

internal suspend fun signTransaction(message: ByteArray, signer: Account): Result<ByteArray, E> {
  return signer.sign(message)
}

@OptIn(ExperimentalEncodingApi::class)
internal suspend fun signAndSubmitTransaction(
  config: SuiConfig,
  signer: Account,
  ptb: ProgrammableTransaction,
  gasBudget: ULong,
  options: ExecuteTransactionBlockResponseOptions,
): Result<ExecuteTransactionBlockMutation.Data?, SuiError> {

  val gasPrice =
    when (val gp = getReferenceGasPrice(config)) {
      is Result.Ok -> gp.value
      is Result.Err -> throw SuiException("Failed to get gas price")
    }

  val paymentObject =
    when (val po = getCoins(config, signer.address)) {
      is Result.Ok -> po.value
      is Result.Err -> throw SuiException("Failed to get payment object")
    }

  val coins =
    paymentObject
      ?.address
      ?.objects
      ?.nodes
      ?.map {
        ObjectReference(
          Reference(AccountAddress.fromString(it.address.toString())),
          it.version.toString().toLong(),
          ObjectDigest(Digest(it.digest.toString())),
        )
      }
      .takeUnless { it.isNullOrEmpty() } ?: throw SuiException("Failed to get payment object")

  val txData =
    TransactionDataComposer.programmable(
      sender = signer.address,
      gasPayment = coins,
      pt = ptb,
      gasBudget = gasBudget,
      gasPrice =
        gasPrice?.epoch?.referenceGasPrice.toString().toULong()
          ?: throw SuiException("Failed to get gas price"),
    )

  val intentMessage = IntentMessage(Intent.suiTransaction(), txData)

  val serializedSignatureBytes: ByteArray =
    when (val sig = signer.sign(hash(Hash.BLAKE2B256, Bcs.encodeToByteArray(intentMessage)))) {
      is Result.Ok -> {
        when (signer.scheme) {
          SignatureScheme.PASSKEY -> sig.value
          else -> byteArrayOf(signer.scheme.scheme) + sig.value + signer.publicKey.data
        }
      }
      is Result.Err -> {
        throw sig.error
      }
    }

  val tx = txData with listOf(Base64.encode(serializedSignatureBytes))

  val content = tx.content()

  val response =
    getGraphqlClient(config)
      .mutation(
        ExecuteTransactionBlockMutation(
          transactionDataBcs = content.first,
          signatures = content.second,
          showBalanceChanges = Optional.presentIfNotNull(options.showBalanceChanges),
          showEffects = Optional.presentIfNotNull(options.showEffects),
          showRawEffects = Optional.presentIfNotNull(options.showRawEffects),
          showEvents = Optional.presentIfNotNull(options.showEvents),
          showInput = Optional.presentIfNotNull(options.showInput),
          showObjectChanges = Optional.presentIfNotNull(options.showObjectChanges),
          showRawInput = Optional.presentIfNotNull(options.showRawInput),
        )
      )
      .execute()

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  return Result.Ok(response.data)
}

/**
 * Waits for a transaction block to be processed and available on the network by polling the
 * `getTransactionBlock` API.
 *
 * This is a crucial utility to use after `signAndExecuteTransactionBlock` to ensure a transaction's
 * effects are finalized and available for querying before proceeding. This implementation uses
 * idiomatic Kotlin coroutines for robust handling of timeouts, polling, and cancellation.
 *
 * @param digest The digest of the transaction to wait for.
 * @param options The options object specifying which fields to return (e.g., effects, object
 *   changes).
 * @param timeout The total time in milliseconds to wait for the transaction before failing.
 *   Defaults to 60 seconds.
 * @param pollInterval The time in milliseconds to wait between each polling attempt. Defaults to 2
 *   seconds.
 * @return A [Result.Ok] containing the transaction block response once it's found, or a
 *   [Result.Err] if the operation times out or encounters an unrecoverable error.
 */
suspend fun waitForTransaction(
  config: SuiConfig,
  digest: String,
  options: TransactionBlockResponseOptions,
  timeout: Long = 10000,
  pollInterval: Long = 2_000L,
): Result<GetTransactionBlockQuery.Data?, SuiError> {
  return try {
    withTimeout(timeout) {
      while (true) {
        when (val result = getTransactionBlock(config, digest, options)) {
          is Result.Ok -> return@withTimeout result
          is Result.Err -> {
            delay(pollInterval)
          }
        }
      }
    }
    Result.Err(SuiError(errors = listOf(GraphQLError(message = "Unexpected fallthrough"))))
  } catch (e: TimeoutCancellationException) {
    Result.Err(
      SuiError(errors = listOf(GraphQLError(message = "Timed out waiting for transaction $digest")))
    )
  } catch (e: Exception) {
    Result.Err(SuiError(errors = listOf(GraphQLError(message = "Unexpected error: ${e.message}"))))
  }
}
