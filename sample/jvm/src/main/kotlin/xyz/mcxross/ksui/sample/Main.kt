package xyz.mcxross.ksui.sample

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings

suspend fun main() {

  // Create a new instance of Sui with the testnet network. Defaults to DEVNET if not specified.
  val sui = Sui(config = SuiConfig(settings = SuiSettings(Network.TESTNET)))

  val committeeInfo = sui.getCommitteeInfo()

  println("Committee Info for current epoch: $committeeInfo")
}
