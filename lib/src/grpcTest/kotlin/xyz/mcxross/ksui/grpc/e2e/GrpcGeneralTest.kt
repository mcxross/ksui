package xyz.mcxross.ksui.grpc.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.rpc.grpc.GrpcStatusException
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.util.runBlocking

class GrpcGeneralTest :
  StringSpec({
    "Get service info via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.getServiceInfo()

          response.chain.shouldNotBeNull().isNotBlank() shouldBe true
          response.chainId.shouldNotBeNull().isNotBlank() shouldBe true
          response.epoch.shouldNotBeNull()
          response.checkpointHeight.shouldNotBeNull()
        }
      } finally {
        client.close()
      }
    }

    "Get checkpoint via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.getCheckpoint()
          val checkpoint = response.checkpoint.shouldNotBeNull()

          checkpoint.sequenceNumber.shouldNotBeNull()
          checkpoint.digest.shouldNotBeNull().isNotBlank() shouldBe true
        }
      } finally {
        client.close()
      }
    }

    "Get epoch via gRPC" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val response = client.getEpoch()
          val epoch = response.epoch.shouldNotBeNull()

          epoch.epoch.shouldNotBeNull()
          epoch.referenceGasPrice.shouldNotBeNull()
        }
      } finally {
        client.close()
      }
    }

    "Verify signature rejects invalid transaction signature on testnet" {
      val client = SuiGrpcClient.fromConfig(SuiConfig(SuiSettings(network = Network.TESTNET)))
      try {
        runBlocking {
          val failure =
            runCatching { client.verifySignature(byteArrayOf(0x00), byteArrayOf(0x00)) }
              .exceptionOrNull()

          val grpcFailure = failure.shouldBeInstanceOf<GrpcStatusException>()
          val statusText = grpcFailure.message.orEmpty()
          (statusText.contains("INVALID_ARGUMENT") || statusText.contains("INTERNAL")) shouldBe true
        }
      } finally {
        client.close()
      }
    }
  })
