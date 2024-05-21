package xyz.mcxross.ksui.sample

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import xyz.mcxross.ksui.client.EndPoint
import xyz.mcxross.ksui.client.suiHttpClient
import xyz.mcxross.ksui.model.Argument
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.TransactionData
import xyz.mcxross.ksui.model.programmableTx
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

  // Transaction sender
  val sender = SuiAddress("0x8ed3c0fc6eb702973e835bc12067999bee650bb0c0dfa0275781fcfa2dd64a6b")

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
        into = listOf(input(1_000UL))
      }
      transferObjects {
        objects = listOf(splitCoins)
        to = input("0xad4c540df24b7dd907bc0ed2ac957aece5a5710a6413a492c678957c10edgeb4")
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

  val dryRunTransactionBlockResponse = sui.dryRunTransactionBlock(Base64.encode(bcsEncode(data)))

  println("Dry run transaction block response: $dryRunTransactionBlockResponse")
}
