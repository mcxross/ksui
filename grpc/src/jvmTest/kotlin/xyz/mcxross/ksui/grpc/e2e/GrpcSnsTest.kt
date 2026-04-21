package xyz.mcxross.ksui.grpc.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.grpc.TestResources

class GrpcSnsTest :
  StringSpec({
    val alice = TestResources.alice

    "Reverse lookup returns NOT_FOUND when an address has no SuiNS reverse record" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val error = client.reverseLookupName(alice.address).unwrapErr()
          error.errors?.firstOrNull()?.extensions?.get("code") shouldBe "NOT_FOUND"
        }
      } finally {
        client.close()
      }
    }
  })
