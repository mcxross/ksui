package xyz.mcxross.ksui.grpc.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.SUI_TYPE
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.util.runBlocking

class GrpcCoinTest :
  StringSpec({
    val alice = TestResources.alice

    "Get balance via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.getBalance(alice.address, SUI_TYPE)

          val balance = response.balance.shouldNotBeNull()
          balance.coinType shouldBe SUI_TYPE
          balance.balance.shouldNotBeNull()
        }
      } finally {
        client.close()
      }
    }

    "List balances via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.listBalances(alice.address)

          response.balances.isNotEmpty() shouldBe true
          response.balances.first().coinType.shouldNotBeNull().isNotBlank() shouldBe true
        }
      } finally {
        client.close()
      }
    }

    "Get coin info via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.getCoinInfo(SUI_TYPE)

          response.coinType shouldBe SUI_TYPE
          val metadata = response.metadata.shouldNotBeNull()
          metadata.name.shouldNotBeNull().isNotBlank() shouldBe true
          metadata.symbol.shouldNotBeNull().isNotBlank() shouldBe true
        }
      } finally {
        client.close()
      }
    }
  })
