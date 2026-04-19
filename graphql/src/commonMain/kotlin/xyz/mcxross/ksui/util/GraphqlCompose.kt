package xyz.mcxross.ksui.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.model.content
import xyz.mcxross.ksui.model.with
import xyz.mcxross.ksui.ptb.ProgrammableTransaction

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
      sender = account.address,
      gasPayment = coins,
      pt = ptb,
      gasBudget = gasBudget,
      gasPrice = gasPrice?.epoch?.referenceGasPrice.toString().toULong(),
    )

  val tx = txData with listOf(Base64.encode(byteArrayOf(0)))
  return tx.content().first
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun ProgrammableTransaction.compose(
  details: Pair<Account, ULong>,
  sui: Sui = SuiKit.client,
): String = composeTransaction(this, details.first, details.second, sui)

@OptIn(ExperimentalEncodingApi::class)
suspend infix fun ProgrammableTransaction.compose(details: Pair<Account, ULong>): String =
  composeTransaction(this, details.first, details.second, SuiKit.client)
