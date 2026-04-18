package xyz.mcxross.ksui.grpc.e2e

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
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

    "Reverse lookup returns NOT_FOUND when an address has no SuiNS reverse record" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        val error =
          shouldThrow<StatusException> {
            runBlocking { client.reverseLookupName(alice.address) }
          }

        error.status.code shouldBe Status.Code.NOT_FOUND
      } finally {
        client.close()
      }
    }
  })
