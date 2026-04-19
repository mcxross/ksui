package xyz.mcxross.ksui

import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings

object TestResources {
  val sui: Sui by lazy { Sui(SuiConfig(SuiSettings(network = Network.TESTNET))) }

  val alice: Account by lazy { Account.import(PRIVATE_KEY_DATA) }
}
