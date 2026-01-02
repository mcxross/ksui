/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.mcxross.ksui.grpc

import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import java.io.Closeable
import java.net.URI
import kotlinx.rpc.grpc.GrpcClient
import kotlinx.rpc.withService
import kotlin.time.Duration
import sui.rpc.v2.SignatureVerificationService
import sui.rpc.v2.TransactionExecutionService
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

/** JVM-only gRPC client for the Sui full node APIs. */
class SuiGrpcClient private constructor(private val grpcClient: GrpcClient) : Closeable {
  val signatureVerificationService: SignatureVerificationService by lazy { grpcClient.withService() }
  val transactionExecutionService: TransactionExecutionService by lazy { grpcClient.withService() }

  override fun close() {
    grpcClient.shutdown()
  }

  fun shutdownNow() {
    grpcClient.shutdownNow()
  }

  suspend fun awaitTermination(timeout: Duration) {
    grpcClient.awaitTermination(timeout)
  }

  companion object {
    fun fromConfig(
      config: SuiConfig,
      configure: ManagedChannelBuilder<*>.() -> Unit = {},
    ): SuiGrpcClient {
      val endpoint = GrpcEndpoint.fromUrl(config.getRequestUrl(SuiApiType.FULLNODE))
      return connect(
        host = endpoint.host,
        port = endpoint.port,
        usePlaintext = endpoint.usePlaintext,
        headers = config.fullNodeConfig.headers,
        configure = configure,
      )
    }

    fun connect(
      host: String,
      port: Int,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      configure: ManagedChannelBuilder<*>.() -> Unit = {},
    ): SuiGrpcClient {
      val client =
        GrpcClient(host, port) {
          if (usePlaintext) {
            usePlaintext()
          }
          applyHeaders(headers)
          configure()
        }
      return SuiGrpcClient(client)
    }

    fun connectTarget(
      target: String,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      configure: ManagedChannelBuilder<*>.() -> Unit = {},
    ): SuiGrpcClient {
      val client =
        GrpcClient(target) {
          if (usePlaintext) {
            usePlaintext()
          }
          applyHeaders(headers)
          configure()
        }
      return SuiGrpcClient(client)
    }
  }
}

data class GrpcEndpoint(
  val host: String,
  val port: Int,
  val usePlaintext: Boolean,
) {
  companion object {
    fun fromUrl(endpoint: String): GrpcEndpoint {
      val normalized = endpoint.trim()
      val uri =
        runCatching { URI(normalized) }.getOrNull()
          ?: runCatching { URI("https://$normalized") }.getOrNull()
      if (uri != null && !uri.host.isNullOrBlank()) {
        val scheme = uri.scheme?.lowercase()
        val defaultPort =
          when (scheme) {
            "http" -> 80
            "https" -> 443
            "grpc" -> 80
            "grpcs" -> 443
            else -> 443
          }
        val port = if (uri.port == -1) defaultPort else uri.port
        val usePlaintext = scheme == "http" || scheme == "grpc"
        return GrpcEndpoint(uri.host, port, usePlaintext)
      }

      val hostAndPort = normalized.removePrefix("//").split(":")
      val host = hostAndPort.firstOrNull().orEmpty()
      val port = hostAndPort.getOrNull(1)?.toIntOrNull() ?: 443
      return GrpcEndpoint(host, port, usePlaintext = false)
    }
  }
}

private fun ManagedChannelBuilder<*>.applyHeaders(headers: Map<String, Any>?) {
  if (headers.isNullOrEmpty()) return

  val metadata = Metadata()
  headers.forEach { (key, value) ->
    val headerKey = Metadata.Key.of(key.lowercase(), Metadata.ASCII_STRING_MARSHALLER)
    metadata.put(headerKey, value.toString())
  }
  intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
}
