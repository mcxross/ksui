package xyz.mcxross.ksui.grpc.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.util.runBlocking

class GrpcSnsTest :
  StringSpec({
    val alice = TestResources.alice

    "Reverse lookup name service names returns successfully on testnet" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.reverseLookupName(alice.address)

          response.record.targetAddress?.let { it shouldBe alice.address.toString() }
        }
      } finally {
        client.close()
      }
    }
  })
