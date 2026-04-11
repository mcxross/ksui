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

package xyz.mcxross.ksui.grpc

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.bytestring.ByteString
import sui.rpc.v2.BcsInternal
import sui.rpc.v2.ExecuteTransactionRequestInternal
import sui.rpc.v2.TransactionInternal
import sui.rpc.v2.UserSignatureInternal
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.generated.GetTransactionBlockQuery
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.Intent
import xyz.mcxross.ksui.model.IntentMessage
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionData
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.bcsEncode
import xyz.mcxross.ksui.util.encodeToBase58String
import kotlin.time.Duration.Companion.milliseconds

private const val ALICE_PRIVATE_KEY =
  "suiprivkey1qqtp4ugtv40c6tj4a7r4vd8ft4nykpxsrh07yqssklraxy243us5qyczx9z"
private val ALICE_ACCOUNT = Account.import(ALICE_PRIVATE_KEY)

private const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

class SuiGrpcTransactionExecutionLiveTest {
  @Test
  fun submitsMoveCallViaGrpc() = runBlocking {
    val client = SuiGrpcClient.connect("fullnode.testnet.sui.io", 443)
    val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)))
    try {
      val ptb = ptb {
        moveCall {
          target = HELLO_WORLD
          arguments = listOf(pure(0UL))
        }
      }

      val gasPrice = fetchReferenceGasPrice(sui)
      val coins = fetchGasCoins(sui, ALICE_ACCOUNT)

      val txData =
        TransactionDataComposer.programmable(
          sender = ALICE_ACCOUNT.address,
          gasPayment = coins,
          pt = ptb,
          gasBudget = 15_000_000UL,
          gasPrice = gasPrice,
        )

      val signatureBytes = txData.signBytes(ALICE_ACCOUNT)
      val txBytes = bcsEncode(txData)
      val txDigest = hash(Hash.BLAKE2B256, txBytes).encodeToBase58String()

      val request =
        ExecuteTransactionRequestInternal().apply {
          transaction =
            TransactionInternal().apply {
              bcs = BcsInternal().apply { value = ByteString(txBytes) }
            }
          signatures =
            listOf(
              UserSignatureInternal().apply {
                bcs = BcsInternal().apply { value = ByteString(signatureBytes) }
              }
            )
        }

      val response = client.transactionExecutionService.ExecuteTransaction(request)
      assertNotNull(response)
      val waited = waitForTransactionByDigest(sui, txDigest)
      assertNotNull(waited)
    } finally {
      client.close()
    }
  }
}

private suspend fun fetchReferenceGasPrice(sui: Sui, attempts: Int = 3): ULong {
  var lastError: String? = null
  repeat(attempts) { attempt ->
    when (val price = sui.getReferenceGasPrice()) {
      is Result.Ok -> {
        val value = price.value?.epoch?.referenceGasPrice
        if (value != null) return value.toString().toULong()
        lastError = "missing reference gas price in response"
      }
      is Result.Err -> lastError = price.error.toString()
    }
    if (attempt < attempts - 1) delay(1_000.milliseconds)
  }
  throw SuiException("Failed to get gas price: ${lastError ?: "unknown error"}")
}

private suspend fun fetchGasCoins(
  sui: Sui,
  account: Account,
  attempts: Int = 5,
): List<ObjectReference> {
  var lastError: String? = null
  repeat(attempts) { attempt ->
    when (val po = sui.getCoins(account.address)) {
      is Result.Ok -> {
        val coins =
          po.value
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
            .orEmpty()
        if (coins.isNotEmpty()) return coins
        lastError = "no gas coins returned"
      }
      is Result.Err -> lastError = po.error.toString()
    }
    if (attempt == 0) {
      sui.requestTestTokens(account.address)
    }
    delay(2_000)
  }
  throw SuiException("Failed to get payment object: ${lastError ?: "unknown error"}")
}

private suspend fun waitForTransactionByDigest(
  sui: Sui,
  digest: String,
  timeout: Long = 60_000,
  pollInterval: Long = 2_000,
): GetTransactionBlockQuery.Data? {
  return try {
    withTimeout(timeout) {
      while (true) {
        when (val result = sui.getTransactionBlock(digest, TransactionBlockResponseOptions())) {
          is Result.Ok -> return@withTimeout result.value
          is Result.Err -> delay(pollInterval)
        }
      }
      null
    }
  } catch (e: Exception) {
    throw SuiException("Transaction not found after gRPC submit: ${e.message}")
  }
}

private suspend fun TransactionData.signBytes(signer: Account): ByteArray {
  val intentMessage = IntentMessage(Intent.suiTransaction(), this)
  val messageHash = hash(Hash.BLAKE2B256, bcsEncode(intentMessage))
  return when (val sig = signer.sign(messageHash)) {
    is Result.Ok ->
      when (signer.scheme) {
        SignatureScheme.PASSKEY -> sig.value
        else -> byteArrayOf(signer.scheme.scheme) + sig.value + signer.publicKey.data
      }
    is Result.Err -> throw sig.error
  }
}
