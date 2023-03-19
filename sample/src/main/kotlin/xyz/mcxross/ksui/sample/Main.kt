package xyz.mcxross.ksui.sample

import xyz.mxcross.ksui.EndPoint
import xyz.mxcross.ksui.SuiAddress
import xyz.mxcross.ksui.createSuiHttpClient

suspend fun main() {
  val suiRpcClient = createSuiHttpClient {
    endpoint = EndPoint.DEVNET
    agentName = "KSUI/0.0.1"
    maxRetries = 10
  }
  println("Balance: " + suiRpcClient.getBalance(SuiAddress("0x3b1db4d4ea331281835e2b450312f82fc4ab880a")))
  println("Coin meta data: " + suiRpcClient.getCoinMetadata("0x2::sui::SUI"))
}
