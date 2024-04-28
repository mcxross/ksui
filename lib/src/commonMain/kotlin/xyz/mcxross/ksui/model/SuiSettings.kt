package xyz.mcxross.ksui.model

data class SuiSettings(
  val network: Network? = null,
  val fullNode: String? = null,
  val faucet: String? = null,
  val indexer: String? = null,
  val fullNodeConfig: FullNodeConfig? = null,
  val indexerConfig: IndexerConfig? = null,
  val faucetConfig: FaucetConfig? = null,
)
