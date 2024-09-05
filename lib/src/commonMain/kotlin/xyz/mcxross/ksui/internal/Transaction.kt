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

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.generated.DryRunTransactionBlock
import xyz.mcxross.ksui.generated.ExecuteTransactionBlock
import xyz.mcxross.ksui.generated.GetTotalTransactionBlocks
import xyz.mcxross.ksui.generated.QueryTransactionBlocks
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.model.Intent
import xyz.mcxross.ksui.model.IntentMessage
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionBlocks
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.model.content
import xyz.mcxross.ksui.model.with
import xyz.mcxross.ksui.ptb.ProgrammableTransaction

suspend fun executeTransactionBlock(
  config: SuiConfig,
  txnBytes: String,
  signatures: List<String>,
  option: ExecuteTransactionBlockResponseOptions,
): Option.Some<ExecuteTransactionBlock.Result?> {
  val response =
    getGraphqlClient(config)
      .execute<ExecuteTransactionBlock.Result>(
        ExecuteTransactionBlock(
          ExecuteTransactionBlock.Variables(txBytes = txnBytes, signatures = signatures)
        )
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  return Option.Some(response.data)
}

internal suspend fun dryRunTransactionBlock(
  config: SuiConfig,
  txnBytes: String,
  option: ExecuteTransactionBlockResponseOptions,
): Option.Some<DryRunTransactionBlock.Result?> {
  val response =
    getGraphqlClient(config)
      .execute<DryRunTransactionBlock.Result>(
        DryRunTransactionBlock(DryRunTransactionBlock.Variables(txBytes = txnBytes))
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  return Option.Some(response.data)
}

suspend fun getTotalTransactionBlocks(config: SuiConfig): Option<Long?> {

  val response =
    getGraphqlClient(config).execute<GetTotalTransactionBlocks.Result>(GetTotalTransactionBlocks())

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data?.checkpoint?.networkTotalTransactions?.toLong())
}

suspend fun queryTransactionBlocks(
  config: SuiConfig,
  transactionBlockResponseOptions: TransactionBlockResponseOptions,
): Option<TransactionBlocks> {
  val response =
    getGraphqlClient(config)
      .execute<QueryTransactionBlocks.Result>(
        QueryTransactionBlocks(
          QueryTransactionBlocks.Variables(
            first = transactionBlockResponseOptions.first,
            last = transactionBlockResponseOptions.last,
            before = transactionBlockResponseOptions.before,
            after = transactionBlockResponseOptions.after,
            showBalanceChanges = transactionBlockResponseOptions.showBalanceChanges,
            showEffects = transactionBlockResponseOptions.showEffects,
            showRawEffects = transactionBlockResponseOptions.showRawEffects,
            showEvents = transactionBlockResponseOptions.showEvents,
            showInput = transactionBlockResponseOptions.showInput,
            showObjectChanges = transactionBlockResponseOptions.showObjectChanges,
            showRawInput = transactionBlockResponseOptions.showRawInput,
            filter = transactionBlockResponseOptions.filter,
          )
        )
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  if (response.data == null) {
    return Option.None
  }

  return Option.Some(response.data)
}

internal fun signTransaction(message: ByteArray, signer: Account): ByteArray {
  return signer.sign(message)
}

@OptIn(ExperimentalEncodingApi::class)
internal suspend fun signAndSubmitTransaction(
  config: SuiConfig,
  signer: Account,
  ptb: ProgrammableTransaction,
  gasBudget: ULong,
): Option.Some<ExecuteTransactionBlock.Result?> {

  val gasPrice =
    when (val gp = getReferenceGasPrice(config)) {
      is Option.Some -> gp.value
      is Option.None -> throw SuiException("Failed to get gas price")
    }

  val coins = getCoins(config, signer.address).expect("Failed to get payment coins")

  val paymentCoins =
    coins.map {
      ObjectReference.from(
        id = AccountAddress.fromString(it.address),
        version = it.version,
        digest = it.digest ?: throw SuiException("Failed to get digest"),
      )
    }

  val txData =
    TransactionDataComposer.programmable(
      sender = signer.address,
      gapPayment = paymentCoins,
      pt = ptb,
      gasBudget = gasBudget,
      gasPrice = gasPrice?.toULong() ?: throw SuiException("Failed to get gas price"),
    )

  val intentMessage = IntentMessage(Intent.suiTransaction(), txData)

  val sig = signer.sign(hash(Hash.BLAKE2B256, Bcs.encodeToByteArray(intentMessage)))

  val serializedSignatureBytes = byteArrayOf(signer.scheme.scheme) + sig + signer.publicKey.data

  val tx = txData with listOf(Base64.encode(serializedSignatureBytes))

  val content = tx.content()

  val response =
    getGraphqlClient(config)
      .execute<ExecuteTransactionBlock.Result>(
        ExecuteTransactionBlock(
          ExecuteTransactionBlock.Variables(txBytes = content.first, signatures = content.second)
        )
      )

  if (response.errors != null) {
    throw SuiException(response.errors.toString())
  }

  return Option.Some(response.data)
}
