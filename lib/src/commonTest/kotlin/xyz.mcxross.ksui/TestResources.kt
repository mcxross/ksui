package xyz.mcxross.ksui

import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings

object TestResources {
  val sui: Sui by lazy { Sui(SuiConfig(SuiSettings(network = Network.TESTNET))) }

  val alice: Account by lazy { Account.import(PRIVATE_KEY_DATA) }
}
