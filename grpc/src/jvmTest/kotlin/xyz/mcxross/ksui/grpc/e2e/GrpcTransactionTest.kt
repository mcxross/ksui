package xyz.mcxross.ksui.grpc.e2e

import com.google.protobuf.kotlin.FieldMask
import com.google.protobuf.kotlin.FieldMaskInternal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.core.exception.SuiException
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.Intent
import xyz.mcxross.ksui.core.model.IntentMessage
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.model.TransactionDataComposer
import xyz.mcxross.ksui.core.ptb.ptb
import xyz.mcxross.ksui.core.util.bcsEncode
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.grpc.SUI_TYPE
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.grpc.TestResources

private const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

class GrpcTransactionTest :
  StringSpec({
    val alice = TestResources.alice

    "Simulate transaction block via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val txData = buildHelloWorldTransactionData(client, alice)
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
          val txData = buildHelloWorldTransactionData(client, alice)
          val signatureBytes = txData.signBytes(TestResources.alice)
          val txBytes = bcsEncode(txData)

          val txDigest =
            client
              .executeTransaction(
                txBytes,
                listOf(signatureBytes),
                readMask("digest", "effects.status", "checkpoint"),
              )
              .unwrap()
              .transaction
              .digest
              ?.takeIf { it.isNotBlank() }
              ?: throw SuiException("gRPC execute response did not include transaction digest")

          waitForTransactionByDigest(client, txDigest).shouldNotBeNull()
        }
      } finally {
        client.close()
      }
    }
  })

private suspend fun buildHelloWorldTransactionData(
  client: SuiGrpcClient,
  alice: Account,
): TransactionData {
  val ptb = ptb {
    moveCall {
      target = HELLO_WORLD
      arguments = listOf(pure(0UL))
    }
  }

  val gasPrice = fetchReferenceGasPrice(client)
  val coins = fetchGasCoins(client, alice.address)
  return TransactionDataComposer.programmable(
    sender = alice.address,
    gasPayment = coins,
    pt = ptb,
    gasBudget = 15_000_000UL,
    gasPrice = gasPrice,
  )
}

private suspend fun fetchReferenceGasPrice(client: SuiGrpcClient, attempts: Int = 3): ULong {
  var lastError: String? = null
  repeat(attempts) { attempt ->
    when (val price = client.getEpoch()) {
      is Result.Ok -> {
        val value = price.value.epoch?.referenceGasPrice
        if (value != null) return value
        lastError = "missing reference gas price in response"
      }
      is Result.Err -> lastError = price.error.toString()
    }
    if (attempt < attempts - 1) delay(1_000)
  }
  throw SuiException("Failed to get gas price: ${lastError ?: "unknown error"}")
}

private suspend fun fetchGasCoins(
  client: SuiGrpcClient,
  sender: AccountAddress,
  attempts: Int = 5,
): List<ObjectReference> {
  var lastError: String? = null
  repeat(attempts) { attempt ->
    when (val po = client.listOwnedObjects(sender, objectType = "0x2::coin::Coin<$SUI_TYPE>")) {
      is Result.Ok -> {
        val coins =
          po.value.objects.mapNotNull { ownedObject ->
            val objectId = ownedObject.objectId ?: return@mapNotNull null
            val version = ownedObject.version ?: return@mapNotNull null
            val digest =
              ownedObject.digest
                ?: client.getObject(objectId, version).unwrap().`object`?.digest
                ?: return@mapNotNull null
            ObjectReference(
              Reference(AccountAddress.fromString(objectId)),
              version.toLong(),
              ObjectDigest(Digest(digest)),
            )
          }
        if (coins.isNotEmpty()) return coins
        lastError = "no gas coins returned"
      }
      is Result.Err -> lastError = po.error.toString()
    }
    delay(2_000)
  }
  throw SuiException("Failed to get payment object: ${lastError ?: "unknown error"}")
}

private suspend fun waitForTransactionByDigest(
  client: SuiGrpcClient,
  digest: String,
  timeout: Long = 60_000,
  pollInterval: Long = 2_000,
): sui.rpc.v2.GetTransactionResponse {
  var lastError: String? = null
  return try {
    withTimeout(timeout) {
      while (true) {
        when (
          val result =
            client.getTransaction(digest, readMask("digest", "effects.status", "checkpoint"))
        ) {
          is Result.Ok -> {
            val transaction = result.value.transaction
            if (transaction.digest == digest) return@withTimeout result.value
            lastError = "response did not include expected digest"
          }
          is Result.Err -> lastError = result.error.toString()
        }
        delay(pollInterval)
      }
      error("unreachable")
    }
  } catch (e: Exception) {
    throw SuiException(
      "Transaction not found after gRPC submit: ${e.message}; last error: ${lastError ?: "none"}"
    )
  }
}

private fun readMask(vararg paths: String): FieldMask =
  FieldMaskInternal().apply { this.paths = paths.toList() }

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
