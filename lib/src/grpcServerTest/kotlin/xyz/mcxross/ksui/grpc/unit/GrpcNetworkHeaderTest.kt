package xyz.mcxross.ksui.grpc.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.rpc.grpc.get
import kotlinx.rpc.grpc.server.GrpcServer
import kotlinx.rpc.grpc.server.GrpcServerCallScope
import kotlinx.rpc.grpc.server.GrpcServerInterceptor
import kotlinx.rpc.registerService
import sui.rpc.v2.SignatureVerificationService
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponse
import sui.rpc.v2.VerifySignatureResponseInternal
import xyz.mcxross.ksui.grpc.SuiGrpcClient
import xyz.mcxross.ksui.model.FullNodeConfig
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.util.runBlocking
import kotlin.time.Duration.Companion.seconds

class GrpcNetworkHeaderTest :
  StringSpec({
    "gRPC request should include custom headers from SuiConfig" {
      var headerValue: String? = null
      val port = unusedTcpPort()
      val server =
        GrpcServer(port) {
            intercept(
              object : GrpcServerInterceptor {
                override fun <Request, Response> GrpcServerCallScope<Request, Response>.intercept(
                  request: Flow<Request>
                ): Flow<Response> =
                  flow {
                    headerValue = requestHeaders["x-custom-header"]
                    proceedUnmodified(request)
                  }
              }
            )
            services {
              registerService<SignatureVerificationService> {
                HeaderAwareSignatureVerificationService()
              }
            }
          }
          .start()

      val config =
        SuiConfig(
          SuiSettings(
            network = Network.CUSTOM,
            fullNode = "http://localhost:$port",
            fullNodeConfig = FullNodeConfig(headers = mapOf("X-Custom-Header" to "CustomValue")),
          )
        )
      val client = SuiGrpcClient.fromConfig(config)

      try {
        runBlocking {
          val response = client.verifySignature(byteArrayOf(0x00), byteArrayOf(0x00)).unwrap()
          response.isValid shouldBe true
        }
        headerValue shouldBe "CustomValue"
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

private class HeaderAwareSignatureVerificationService : SignatureVerificationService {
  override suspend fun VerifySignature(message: VerifySignatureRequest): VerifySignatureResponse {
    return VerifySignatureResponseInternal().apply { isValid = true }
  }
}
