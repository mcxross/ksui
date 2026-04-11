package xyz.mcxross.ksui.grpc.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.io.bytestring.ByteString
import kotlinx.rpc.grpc.GrpcStatusException
import sui.rpc.v2.BcsInternal
import sui.rpc.v2.GetCheckpointRequestInternal
import sui.rpc.v2.GetEpochRequestInternal
import sui.rpc.v2.GetServiceInfoRequestInternal
import sui.rpc.v2.UserSignatureInternal
import sui.rpc.v2.VerifySignatureRequestInternal
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
          val response = client.ledgerService.GetServiceInfo(GetServiceInfoRequestInternal())

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
          val response = client.ledgerService.GetCheckpoint(GetCheckpointRequestInternal())
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
          val response = client.ledgerService.GetEpoch(GetEpochRequestInternal())
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
            runCatching {
              client.signatureVerificationService.VerifySignature(invalidVerifySignatureRequest())
            }.exceptionOrNull()

          val grpcFailure = failure.shouldBeInstanceOf<GrpcStatusException>()
          val statusText = grpcFailure.message.orEmpty()
          (statusText.contains("INVALID_ARGUMENT") || statusText.contains("INTERNAL")) shouldBe true
        }
      } finally {
        client.close()
      }
    }
  })


private fun invalidVerifySignatureRequest(): VerifySignatureRequestInternal {
  val message =
    BcsInternal().apply {
      name = "TransactionData"
      value = ByteString(byteArrayOf(0x00))
    }
  val signature =
    UserSignatureInternal().apply {
      bcs =
        BcsInternal().apply {
          name = "UserSignature"
          value = ByteString(byteArrayOf(0x00))
        }
    }

  return VerifySignatureRequestInternal().apply {
    this.message = message
    this.signature = signature
  }
}
