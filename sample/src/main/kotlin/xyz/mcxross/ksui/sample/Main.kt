package xyz.mcxross.ksui.sample

import xyz.mxcross.ksui.EndPoint
import xyz.mxcross.ksui.createSuiRpcClient

suspend fun main() {
  val suiRpcClient = createSuiRpcClient {
    setEndPoint(EndPoint.DEVNET)
  }
  suiRpcClient.getValidators().list.forEach { println(it) }
}
