package xyz.mcxross.ksui.android.controller

import android.util.Log
import xyz.mcxross.ksui.client.EndPoint
import xyz.mcxross.ksui.client.suiHttpClient
import xyz.mcxross.ksui.extension.asObjectReference
import xyz.mcxross.ksui.model.Argument
import xyz.mcxross.ksui.model.IntentMessage
import xyz.mcxross.ksui.model.IntentType
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.TransactionDataComposer
import xyz.mcxross.ksui.model.programmableTx
import xyz.mcxross.ksui.util.bcs
import xyz.mcxross.ksui.util.fullTxBlockResponseOptions
import xyz.mcxross.ksui.util.inputs
import xyz.mcxross.sc.SuiCommons

suspend fun send(sk: String, from: SuiAddress, toSuiAddress: SuiAddress) {
  val suiRpcClient = suiHttpClient {
    endpoint = EndPoint.DEVNET
    agentName = "KSUI/0.0.1"
    maxRetries = 10
  }

  val paymentObject = suiRpcClient.getCoins(SuiAddress(from.pubKey), limit = 5)

  Log.d("MainActivity", paymentObject.data.map { it.asObjectReference() }.toString())

  val gasPrice = suiRpcClient.getReferenceGasPrice()

  Log.d("MainActivity", "Reference Gas Price: $gasPrice")

  val pt = programmableTx {
    command {
      val splitCoins = splitCoins {
        coin = Argument.GasCoin
        into = inputs(1_000.toULong())
      }

      /*transferObjects {
        objects = inputs(splitCoins)
        to = input(toSuiAddress.pubKey)
      }*/
    }
  }

  val txData =
    TransactionDataComposer.programmable(
      from,
      listOf(paymentObject.data[0].asObjectReference()),
      pt,
      5_000_000UL,
      gasPrice.cost.toULong(),
    )

  val intentMessage = IntentMessage(IntentType.SUI_TX.intent(), txData)

  val sig = SuiCommons.utils.suiSign(bcs(intentMessage), sk)

  Log.d("MainActivity", "Signature: $sig")

  Log.d("MainActivity", "Sending transaction")

  val result =
    suiRpcClient.executeTransactionBlock(
      SuiCommons.encode.encodeBase64(bcs(intentMessage)),
      listOf(sig),
      fullTxBlockResponseOptions(),
    )

  Log.d("MainActivity", "Transaction result: $result")
}
