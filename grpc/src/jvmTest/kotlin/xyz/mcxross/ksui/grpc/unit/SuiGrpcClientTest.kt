package xyz.mcxross.ksui.grpc.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.registerService
import sui.rpc.v2.SignatureVerificationService
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponse
import sui.rpc.v2.VerifySignatureResponseInternal
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.util.runBlocking

class SuiGrpcClientTest :
  StringSpec({
    "Client uses signature verification service" {
      val port = unusedTcpPort()
      val server =
        GrpcServer(port) {
            services {
              registerService<SignatureVerificationService> { TestSignatureVerificationService() }
            }
          }
          .start()
      val client = SuiGrpcClient.connect("localhost", port, usePlaintext = true)

      try {
        runBlocking {
          val response = client.verifySignature(byteArrayOf(0x00), byteArrayOf(0x00)).unwrap()
          response.isValid shouldBe true
        }
      } finally {
        client.close()
        server.shutdown()
        runBlocking {
          client.awaitTermination(2.seconds)
          server.awaitTermination(2.seconds)
        }
      }
    }
  })

private class TestSignatureVerificationService : SignatureVerificationService {
  override suspend fun VerifySignature(message: VerifySignatureRequest): VerifySignatureResponse {
    return VerifySignatureResponseInternal().apply { isValid = true }
  }
}
