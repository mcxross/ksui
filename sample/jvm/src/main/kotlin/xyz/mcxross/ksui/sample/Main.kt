package xyz.mcxross.ksui.sample

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.bouncycastle.jcajce.provider.digest.Blake2b
import xyz.mcxross.ksui.client.EndPoint
import xyz.mcxross.ksui.client.suiHttpClient
import xyz.mcxross.ksui.model.Argument
import xyz.mcxross.ksui.model.Intent
import xyz.mcxross.ksui.model.IntentMessage
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionData
import xyz.mcxross.ksui.model.content
import xyz.mcxross.ksui.model.programmableTx
import xyz.mcxross.ksui.model.with
import xyz.mcxross.ksui.util.bcsEncode
import xyz.mcxross.ksui.util.requestTestTokens

fun isTestTokensAvailable(endpoint: EndPoint): Boolean {
  return endpoint == EndPoint.TESTNET || endpoint == EndPoint.DEVNET
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun main() {

  val sui = suiHttpClient {
    endpoint = EndPoint.DEVNET
    agentName = "KSUI/0.0.1"
    maxRetries = 10
  }

  val suiKeyPair =
    importFromMnemonic(
      "share usual miss chair champion issue rally rifle train talent among endless"
    )

  // Transaction sender
  val sender = SuiAddress(suiKeyPair.address())
  println("Sender balance: ${sui.getBalance(sender)}")

  val senderCoins = sui.getCoins(sender)

  if (senderCoins.data.isEmpty() && isTestTokensAvailable(sui.configContainer.endPoint)) {
    println("Requesting test tokens")
    sui.requestTestTokens(sender)
  } else if (senderCoins.data.isEmpty()) {
    println("No coins available and not testnet or devnet so exiting...")
    return
  }

  val gasPrice = sui.getReferenceGasPrice()

  // Now, we can create a programmable transaction. In this example, we will create a transaction
  // that splits the Gas coin into one of 1_000 and the other of the rest. We use Gas coin because
  // it is the
  // always available for each transaction, but you can use any other coin
  val ptb = programmableTx {
    command {
      val splitCoins = splitCoins {
        coin = Argument.GasCoin
        into = listOf(input(1_000_000_000UL))
      }
      transferObjects {
        objects = listOf(splitCoins)
        to = inputStr("0xa087ff13a8e27a85a54db187c01de5b6f6a10f9ae74c86ce25620374a05f2f1f")
      }
    }
  }

  // Now, we can create the transaction data
  val data =
    TransactionData.programmable(
      sender,
      listOf(senderCoins pick 0),
      ptb,
      5_000_000UL,
      gasPrice.cost.toULong(),
    )

  // val d = sui.dryRunTransactionBlock(Base64.encode(bcsEncode(data)))

  // println(d)

  // We sign the intent message
  val intentMessage = IntentMessage(Intent.suiTransaction(), data)

  val sig = suiKeyPair.sign(Blake2b.Blake2b256().digest(bcsEncode(intentMessage)))

  val serializedSignatureBytes = byteArrayOf(0) + sig + suiKeyPair.publicKeyBytes()!!

  val sigBase64 = Base64.encode(serializedSignatureBytes)

  val tx = data with listOf(sigBase64)

  val content = tx.content()

  val transactionBlockResponse =
    sui.executeTransactionBlock(
      content.first,
      content.second,
      TransactionBlockResponseOptions(
        showInput = true,
        showRawInput = true,
        showEffects = true,
        showEvents = true,
        showObjectChanges = true,
        showBalanceChanges = true,
      ),
    )

  println("Transaction block response: $transactionBlockResponse")

  val receiptBalance =
    sui.getBalance(SuiAddress("0xa087ff13a8e27a85a54db187c01de5b6f6a10f9ae74c86ce25620374a05f2f1f"))

  println("Receiver balance: $receiptBalance")
}
