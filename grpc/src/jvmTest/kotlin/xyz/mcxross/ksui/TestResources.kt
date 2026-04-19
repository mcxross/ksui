package xyz.mcxross.ksui

import xyz.mcxross.ksui.account.Account

object TestResources {
  val alice: Account by lazy { Account.import(PRIVATE_KEY_DATA) }
}
