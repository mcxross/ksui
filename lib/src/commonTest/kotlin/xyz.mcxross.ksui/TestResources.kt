package xyz.mcxross.ksui

import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.util.runBlocking

object TestResources {
  val sui: Sui by lazy { Sui(SuiConfig(SuiSettings(network = Network.LOCAL))) }

  val alice: Account by lazy {
    val account = Account.create()
    runBlocking { sui.requestTestTokens(account.address) }
    account
  }
}
