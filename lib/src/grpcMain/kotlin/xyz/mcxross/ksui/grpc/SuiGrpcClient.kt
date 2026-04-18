/*
 * Copyright 2025 McXross
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

import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.grpc.append
import kotlinx.rpc.grpc.client.GrpcClient
import kotlinx.rpc.grpc.client.GrpcClientCallScope
import kotlinx.rpc.grpc.client.GrpcClientConfiguration
import kotlinx.rpc.grpc.client.GrpcClientInterceptor
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.protocol.Coin
import xyz.mcxross.ksui.grpc.protocol.General
import xyz.mcxross.ksui.grpc.protocol.Move
import xyz.mcxross.ksui.grpc.protocol.Object
import xyz.mcxross.ksui.grpc.protocol.Signature
import xyz.mcxross.ksui.grpc.protocol.Sns
import xyz.mcxross.ksui.grpc.protocol.Subscription
import xyz.mcxross.ksui.grpc.protocol.Transaction
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

/** gRPC client for the ksui targets supported by kotlinx-rpc gRPC. */
class SuiGrpcClient private constructor(private val runtime: GrpcRuntime) :
  Coin by xyz.mcxross.ksui.grpc.api.Coin(runtime),
  General by xyz.mcxross.ksui.grpc.api.General(runtime),
  Move by xyz.mcxross.ksui.grpc.api.Move(runtime),
  Object by xyz.mcxross.ksui.grpc.api.Object(runtime),
  Signature by xyz.mcxross.ksui.grpc.api.Signature(runtime),
  Sns by xyz.mcxross.ksui.grpc.api.Sns(runtime),
  Subscription by xyz.mcxross.ksui.grpc.api.Subscription(runtime),
  Transaction by xyz.mcxross.ksui.grpc.api.Transaction(runtime) {
  fun close() {
    runtime.close()
  }

  fun shutdownNow() {
    runtime.shutdownNow()
  }

  suspend fun awaitTermination(timeout: Duration) {
    runtime.awaitTermination(timeout)
  }

  companion object {
    fun fromConfig(
      config: SuiConfig,
      configure: GrpcClientConfiguration.() -> Unit = {},
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
      configure: GrpcClientConfiguration.() -> Unit = {},
    ): SuiGrpcClient {
      val client =
        GrpcClient(host, port) {
          if (usePlaintext) {
            credentials = plaintext()
          }
          applyHeaders(headers)
          configure()
        }
      return SuiGrpcClient(GrpcRuntime(client))
    }

    fun connectTarget(
      target: String,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      configure: GrpcClientConfiguration.() -> Unit = {},
    ): SuiGrpcClient {
      val client =
        GrpcClient(target) {
          if (usePlaintext) {
            credentials = plaintext()
          }
          applyHeaders(headers)
          configure()
        }
      return SuiGrpcClient(GrpcRuntime(client))
    }
  }
}

data class GrpcEndpoint(val host: String, val port: Int, val usePlaintext: Boolean) {
  companion object {
    fun fromUrl(endpoint: String): GrpcEndpoint {
      val normalized = endpoint.trim()
      val withScheme =
        when {
          "://" in normalized -> normalized
          normalized.startsWith("//") -> "https:$normalized"
          else -> "https://$normalized"
        }

      val scheme = withScheme.substringBefore("://", "https").lowercase()
      val authority =
        withScheme
          .substringAfter("://", normalized)
          .substringBefore('/')
          .substringBefore('?')
          .substringBefore('#')

      if (authority.isNotBlank()) {
        val hostAndPort = parseHostAndPort(authority.removePrefix("//"))
        val port = hostAndPort.port ?: defaultPortForScheme(scheme)
        val usePlaintext = scheme == "http" || scheme == "grpc"
        return GrpcEndpoint(hostAndPort.host, port, usePlaintext)
      }

      val hostAndPort = parseHostAndPort(normalized.removePrefix("//"))
      return GrpcEndpoint(hostAndPort.host, hostAndPort.port ?: 443, usePlaintext = false)
    }

    private fun defaultPortForScheme(scheme: String): Int =
      when (scheme) {
        "http",
        "grpc" -> 80
        else -> 443
      }

    private fun parseHostAndPort(authority: String): HostAndPort {
      val value = authority.substringAfterLast('@')
      if (value.startsWith("[")) {
        val end = value.indexOf(']')
        if (end != -1) {
          val host = value.substring(1, end)
          val port = value.substring(end + 1).removePrefix(":").toIntOrNull()
          return HostAndPort(host, port)
        }
      }

      val colonCount = value.count { it == ':' }
      if (colonCount == 1) {
        val separatorIndex = value.lastIndexOf(':')
        return HostAndPort(
          host = value.substring(0, separatorIndex),
          port = value.substring(separatorIndex + 1).toIntOrNull(),
        )
      }

      return HostAndPort(value, null)
    }
  }
}

private data class HostAndPort(val host: String, val port: Int?)

private fun GrpcClientConfiguration.applyHeaders(headers: Map<String, Any>?) {
  if (headers.isNullOrEmpty()) return

  intercept(
    object : GrpcClientInterceptor {
      override fun <Request, Response> GrpcClientCallScope<Request, Response>.intercept(
        request: Flow<Request>
      ): Flow<Response> {
        headers.forEach { (key, value) -> requestHeaders.append(key.lowercase(), value.toString()) }
        return proceed(request)
      }
    }
  )
}
