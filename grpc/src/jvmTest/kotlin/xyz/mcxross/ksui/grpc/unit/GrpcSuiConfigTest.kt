package xyz.mcxross.ksui.grpc.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.grpc.GrpcEndpoint
import xyz.mcxross.ksui.model.FullNodeConfig
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings

class GrpcSuiConfigTest :
  StringSpec({
    "SuiConfig should correctly capture custom gRPC urls and headers" {
      val customSettings =
        SuiSettings(
          network = Network.CUSTOM,
          fullNode = "https://custom-fullnode.com:9443",
          fullNodeConfig =
            FullNodeConfig(headers = mapOf("Authorization" to "Bearer custom-node-api-key")),
        )

      val config = SuiConfig(customSettings)
      val endpoint = GrpcEndpoint.fromUrl(config.getRequestUrl(SuiApiType.FULLNODE))

      config.network shouldBe Network.CUSTOM
      endpoint.host shouldBe "custom-fullnode.com"
      endpoint.port shouldBe 9443
      endpoint.usePlaintext shouldBe false
      config.fullNodeConfig.headers?.get("Authorization") shouldBe "Bearer custom-node-api-key"
    }

    "GrpcEndpoint should default scheme-less urls to tls" {
      val endpoint = GrpcEndpoint.fromUrl("custom-fullnode.com")

      endpoint.host shouldBe "custom-fullnode.com"
      endpoint.port shouldBe 443
      endpoint.usePlaintext shouldBe false
    }

    "GrpcEndpoint should mark http urls as plaintext" {
      val endpoint = GrpcEndpoint.fromUrl("http://custom-fullnode.com")

      endpoint.host shouldBe "custom-fullnode.com"
      endpoint.port shouldBe 80
      endpoint.usePlaintext shouldBe true
    }
  })
