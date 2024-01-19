package xyz.mcxross.ksui.sample

import xyz.mcxross.ksui.client.EndPoint
import xyz.mcxross.ksui.client.suiHttpClient
import xyz.mcxross.ksui.model.SuiAddress

suspend fun main() {
  val suiRpcClient = suiHttpClient {
    endpoint = EndPoint.DEVNET
    agentName = "KSUI/0.0.1"
    maxRetries = 10
  }
  println("Balance: " + suiRpcClient.getBalance(SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b")))
  println("Coin meta data: " + suiRpcClient.getCoinMetadata("0x2::sui::SUI"))
}
