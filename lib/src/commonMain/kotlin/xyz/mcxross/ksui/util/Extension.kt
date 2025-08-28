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
package xyz.mcxross.ksui.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.Intent
import xyz.mcxross.ksui.model.IntentMessage
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.model.TransactionDigest
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.model.content
import xyz.mcxross.ksui.model.with
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.ProgrammableTransactionBuilder

/** Extension function to create a [TransactionDigest] from a [String]. */
fun String.toTxnDigest(): TransactionDigest = TransactionDigest(this)

/** Extension functions to create [Argument.Input]s from various types. */
inline fun <reified T : Any> ProgrammableTransactionBuilder.inputs(
  vararg inputs: T
): List<Argument> = inputs.map { it as? Argument.Result ?: input(it) }

inline fun <reified T : TypeTag> ProgrammableTransactionBuilder.types(
  vararg types: T
): List<TypeTag> = types.toList()

@OptIn(ExperimentalEncodingApi::class)
private suspend fun composeTransaction(
  ptb: ProgrammableTransaction,
  account: Account,
  gasBudget: ULong,
  sui: Sui,
): String {
  val gasPrice =
    when (val gp = sui.getReferenceGasPrice()) {
      is Result.Ok -> gp.value
      is Result.Err -> throw SuiException("Failed to get gas price")
    }

  val paymentObject =
    when (val po = sui.getCoins(account.address)) {
      is Result.Ok -> po.value
      is Result.Err -> throw SuiException("Failed to get payment object")
    }

  val coins =
    paymentObject
      ?.address
      ?.coins
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
      sender = account.address,
      gasPayment = coins,
      pt = ptb,
      gasBudget = gasBudget,
      gasPrice =
        gasPrice?.epoch?.referenceGasPrice.toString().toULong()
          ?: throw SuiException("Failed to get gas price"),
    )

  val intentMessage = IntentMessage(Intent.suiTransaction(), txData)
  val serializedSignatureBytes: ByteArray =
    when (val sig = account.sign(hash(Hash.BLAKE2B256, Bcs.encodeToByteArray(intentMessage)))) {
      is Result.Ok -> {
        when (account.scheme) {
          SignatureScheme.PASSKEY -> sig.value
          else -> byteArrayOf(account.scheme.scheme) + sig.value + account.publicKey.data
        }
      }
      is Result.Err -> {
        throw sig.error
      }
    }

  val tx = txData with listOf(Base64.encode(serializedSignatureBytes))
  val content = tx.content()

  return content.first
}

/**
 * Composes a ProgrammableTransaction into the final transaction bytes string. This is the standard
 * extension function.
 */
@OptIn(ExperimentalEncodingApi::class)
suspend fun ProgrammableTransaction.compose(
  details: Pair<Account, ULong>,
  sui: Sui = SuiKit.client,
): String {
  return composeTransaction(this, details.first, details.second, sui)
}

/**
 * Composes a ProgrammableTransaction into the final transaction bytes string. This is the infix
 * version for a more expressive syntax.
 */
@OptIn(ExperimentalEncodingApi::class)
suspend infix fun ProgrammableTransaction.compose(details: Pair<Account, ULong>): String {
  return composeTransaction(this, details.first, details.second, SuiKit.client)
}
