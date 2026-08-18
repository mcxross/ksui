package xyz.mcxross.ksui.grpc

import xyz.mcxross.ksui.core.account.Account

object TestResources {
  val alice: Account by lazy {
    val key =
      requireNotNull(System.getenv("KSUI_TEST_PRIVATE_KEY")?.takeIf { it.isNotBlank() }) {
        "KSUI_TEST_PRIVATE_KEY is required for network-connected e2e tests"
      }
    Account.import(key)
  }
}
