package xyz.mcxross.ksui.grpc.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.generated.GetTransactionBlockQuery
import xyz.mcxross.ksui.grpc.SuiGrpcClient
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
import xyz.mcxross.ksui.util.runBlocking

private const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

class GrpcTransactionTest :
  StringSpec({
    val sui = TestResources.sui
    val alice = TestResources.alice

    "Simulate transaction block via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val txData = buildHelloWorldTransactionData(sui, alice)
          val txBytes = bcsEncode(txData)

          val response = client.simulateTransaction(txBytes).unwrap()

          response.shouldNotBeNull()
          response.transaction.shouldNotBeNull()
        }
      } finally {
        client.close()
      }
    }

    "Simulate transaction rejects malformed bytes" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val error = client.simulateTransaction(byteArrayOf(0x00)).unwrapErr()
          val statusText = error.toString()
          (statusText.contains("INVALID_ARGUMENT") || statusText.contains("INTERNAL")) shouldBe true
        }
      } finally {
        client.close()
      }
    }

    "Execute transaction block via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val txData = buildHelloWorldTransactionData(sui, alice)
          val signatureBytes = txData.signBytes(TestResources.alice)
          val txBytes = bcsEncode(txData)
          val txDigest = hash(Hash.BLAKE2B256, txBytes).encodeToBase58String()

          client.executeTransaction(txBytes, listOf(signatureBytes)).unwrap().shouldNotBeNull()
          waitForTransactionByDigest(sui, txDigest).shouldNotBeNull()
        }
      } finally {
        client.close()
      }
    }
  })

private suspend fun buildHelloWorldTransactionData(
  sui: xyz.mcxross.ksui.Sui,
  alice: xyz.mcxross.ksui.account.Account,
): TransactionData {
  val ptb = ptb {
    moveCall {
      target = HELLO_WORLD
      arguments = listOf(pure(0UL))
    }
  }

  val gasPrice = fetchReferenceGasPrice(sui)
  val coins = fetchGasCoins(sui, alice.address)
  return TransactionDataComposer.programmable(
    sender = alice.address,
    gasPayment = coins,
    pt = ptb,
    gasBudget = 15_000_000UL,
    gasPrice = gasPrice,
  )
}

private suspend fun fetchReferenceGasPrice(sui: xyz.mcxross.ksui.Sui, attempts: Int = 3): ULong {
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
    if (attempt < attempts - 1) delay(1_000)
  }
  throw SuiException("Failed to get gas price: ${lastError ?: "unknown error"}")
}

private suspend fun fetchGasCoins(
  sui: xyz.mcxross.ksui.Sui,
  sender: xyz.mcxross.ksui.model.AccountAddress,
  attempts: Int = 5,
): List<ObjectReference> {
  var lastError: String? = null
  repeat(attempts) { attempt ->
    when (val po = sui.getCoins(sender)) {
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
      sui.requestTestTokens(sender)
    }
    delay(2_000)
  }
  throw SuiException("Failed to get payment object: ${lastError ?: "unknown error"}")
}

private suspend fun waitForTransactionByDigest(
  sui: xyz.mcxross.ksui.Sui,
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

private suspend fun TransactionData.signBytes(signer: xyz.mcxross.ksui.account.Account): ByteArray {
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
