package xyz.mcxross.ksui

import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings

object TestResources {
  val sui: Sui by lazy { Sui(SuiConfig(SuiSettings(network = Network.TESTNET))) }

  val alice: Account by lazy {
    val key =
      requireNotNull(System.getenv("KSUI_TEST_PRIVATE_KEY")?.takeIf { it.isNotBlank() }) {
        "KSUI_TEST_PRIVATE_KEY is required for network-connected e2e tests"
      }
    Account.import(key)
  }
}
