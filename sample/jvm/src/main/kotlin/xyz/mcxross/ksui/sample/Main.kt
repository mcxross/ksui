package xyz.mcxross.ksui.sample

import xyz.mcxross.ksui.client.EndPoint
import xyz.mcxross.ksui.client.createSuiHttpClient
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.util.requestTestTokens

suspend fun main() {
  val suiRpcClient = createSuiHttpClient {
    endpoint = EndPoint.DEVNET
    agentName = "KSUI/0.0.1"
    maxRetries = 10
  }
  println(
      suiRpcClient.requestTestTokens(
          SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b")))
}
