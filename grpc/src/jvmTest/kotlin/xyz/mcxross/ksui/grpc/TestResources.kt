package xyz.mcxross.ksui.grpc

import xyz.mcxross.ksui.core.account.Account

object TestResources {
  val alice: Account by lazy { Account.import(PRIVATE_KEY_DATA) }
}
