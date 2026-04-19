package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.model.*

class SuiConfigTest :
  StringSpec({
    "SuiConfig should correctly capture custom URLs and headers" {
      val customSettings =
        SuiSettings(
          network = Network.CUSTOM,
          fullNode = "https://custom-fullnode.com",
          indexer = "https://custom-indexer.com",
          fullNodeConfig =
            FullNodeConfig(headers = mapOf("Authorization" to "Bearer custom-node-api-key")),
          indexerConfig = IndexerConfig(headers = mapOf("x-api-key" to "custom-indexer-api-key")),
        )

      val config = SuiConfig(customSettings)

      config.network shouldBe Network.CUSTOM
      config.getRequestUrl(SuiApiType.FULLNODE) shouldBe "https://custom-fullnode.com"
      config.getRequestUrl(SuiApiType.INDEXER) shouldBe "https://custom-indexer.com"

      config.fullNodeConfig.headers?.get("Authorization") shouldBe "Bearer custom-node-api-key"
      config.indexerConfig.headers?.get("x-api-key") shouldBe "custom-indexer-api-key"
    }
  })
