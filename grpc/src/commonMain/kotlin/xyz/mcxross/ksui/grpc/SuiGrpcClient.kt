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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.grpc.append
import kotlinx.rpc.grpc.client.GrpcClient
import kotlinx.rpc.grpc.client.GrpcClientCallScope
import kotlinx.rpc.grpc.client.GrpcClientConfiguration
import kotlinx.rpc.grpc.client.GrpcClientInterceptor
import kotlinx.rpc.grpc.descriptor.GrpcMethodType
import kotlinx.rpc.grpc.descriptor.methodType
import xyz.mcxross.ksui.core.client.ClientConfig
import xyz.mcxross.ksui.core.model.SuiApiType
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.protocol.Coin
import xyz.mcxross.ksui.grpc.protocol.General
import xyz.mcxross.ksui.grpc.protocol.Move
import xyz.mcxross.ksui.grpc.protocol.Object
import xyz.mcxross.ksui.grpc.protocol.Signature
import xyz.mcxross.ksui.grpc.protocol.Sns
import xyz.mcxross.ksui.grpc.protocol.Subscription
import xyz.mcxross.ksui.grpc.protocol.Transaction

class SuiGrpcClient private constructor(private val runtime: GrpcRuntime) :
  AutoCloseable,
  Coin by xyz.mcxross.ksui.grpc.api.Coin(runtime),
  General by xyz.mcxross.ksui.grpc.api.General(runtime),
  Move by xyz.mcxross.ksui.grpc.api.Move(runtime),
  Object by xyz.mcxross.ksui.grpc.api.Object(runtime),
  Signature by xyz.mcxross.ksui.grpc.api.Signature(runtime),
  Sns by xyz.mcxross.ksui.grpc.api.Sns(runtime),
  Subscription by xyz.mcxross.ksui.grpc.api.Subscription(runtime),
  Transaction by xyz.mcxross.ksui.grpc.api.Transaction(runtime) {

  override fun close() {
    runtime.close()
  }

  fun shutdownNow() {
    runtime.shutdownNow()
  }

  @Throws(Exception::class)
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
        requestTimeout = config.clientConfig.requestTimeout.toRequestTimeout(),
        configure = configure,
      )
    }

    /** Connects with a unary-call deadline of [ClientConfig.requestTimeout] (10 seconds by default). */
    fun connect(
      host: String,
      port: Int,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      configure: GrpcClientConfiguration.() -> Unit = {},
    ): SuiGrpcClient = connect(host, port, usePlaintext, headers, DEFAULT_REQUEST_TIMEOUT, configure)

    /**
     * @param requestTimeout deadline for each unary call. Streaming calls such as checkpoint
     *   subscriptions get no deadline. Pass `null` to disable it; an interceptor added in [configure]
     *   can still set its own `callOptions.timeout`.
     */
    fun connect(
      host: String,
      port: Int,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      requestTimeout: Duration?,
      configure: GrpcClientConfiguration.() -> Unit = {},
    ): SuiGrpcClient {
      require(host.isNotBlank()) { "gRPC host must not be blank" }
      require(port in 1..65535) { "gRPC port out of range: $port" }
      val validatedHeaders = validateHeaders(headers)
      val client =
        GrpcClient(host, port) {
          if (usePlaintext) {
            credentials = plaintext()
          }
          applyHeaders(validatedHeaders)
          applyUnaryDeadline(requestTimeout)
          configure()
        }
      return SuiGrpcClient(GrpcRuntime(client))
    }

    /** Connects with a unary-call deadline of [ClientConfig.requestTimeout] (10 seconds by default). */
    fun connectTarget(
      target: String,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      configure: GrpcClientConfiguration.() -> Unit = {},
    ): SuiGrpcClient = connectTarget(target, usePlaintext, headers, DEFAULT_REQUEST_TIMEOUT, configure)

    /** @param requestTimeout deadline for each unary call; see the [connect] overload. */
    fun connectTarget(
      target: String,
      usePlaintext: Boolean = false,
      headers: Map<String, Any>? = null,
      requestTimeout: Duration?,
      configure: GrpcClientConfiguration.() -> Unit = {},
    ): SuiGrpcClient {
      require(target.isNotBlank()) { "gRPC target must not be blank" }
      val validatedHeaders = validateHeaders(headers)
      val client =
        GrpcClient(target) {
          if (usePlaintext) {
            credentials = plaintext()
          }
          applyHeaders(validatedHeaders)
          applyUnaryDeadline(requestTimeout)
          configure()
        }
      return SuiGrpcClient(GrpcRuntime(client))
    }

    private val DEFAULT_REQUEST_TIMEOUT: Duration? = ClientConfig().requestTimeout.toRequestTimeout()
  }
}

private fun Long.toRequestTimeout(): Duration? = takeIf { it > 0 }?.milliseconds

// Without a deadline a stalled node hangs the calling coroutine forever.
private fun GrpcClientConfiguration.applyUnaryDeadline(timeout: Duration?) {
  if (timeout == null || !timeout.isPositive() || timeout.isInfinite()) return
  intercept(
    object : GrpcClientInterceptor {
      override fun <Request, Response> GrpcClientCallScope<Request, Response>.intercept(
        request: Flow<Request>
      ): Flow<Response> {
        if (method.methodType == GrpcMethodType.UNARY && callOptions.timeout == null) {
          callOptions.timeout = timeout
        }
        return proceed(request)
      }
    }
  )
}

data class GrpcEndpoint(val host: String, val port: Int, val usePlaintext: Boolean) {
  init {
    require(host.isNotBlank()) { "gRPC endpoint host must not be blank" }
    require(port in 1..65535) { "gRPC endpoint port out of range: $port" }
  }

  companion object {
    fun fromUrl(endpoint: String): GrpcEndpoint {
      val normalized = endpoint.trim()
      require(normalized.isNotEmpty()) { "gRPC endpoint URL must not be empty" }

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
      require(authority.isNotBlank()) { "Invalid gRPC endpoint URL: $endpoint" }

      val hostAndPort = parseHostAndPort(authority.removePrefix("//"))
      val port = hostAndPort.port ?: defaultPortForScheme(scheme)
      val usePlaintext = scheme == "http" || scheme == "grpc"
      return GrpcEndpoint(hostAndPort.host, port, usePlaintext)
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
        require(end > 1) { "Invalid IPv6 host in endpoint: $authority" }
        val host = value.substring(1, end)
        val remainder = value.substring(end + 1)
        val port =
          if (remainder.isEmpty()) {
            null
          } else {
            require(remainder.startsWith(":")) { "Invalid port suffix in endpoint: $authority" }
            parsePort(remainder.substring(1), authority)
          }
        return HostAndPort(host, port)
      }

      val colonCount = value.count { it == ':' }
      if (colonCount == 1) {
        val separatorIndex = value.lastIndexOf(':')
        val host = value.substring(0, separatorIndex)
        val portStr = value.substring(separatorIndex + 1)
        return HostAndPort(host = host, port = parsePort(portStr, authority))
      }

      return HostAndPort(value, null)
    }

    private fun parsePort(portStr: String, authority: String): Int {
      val parsed =
        portStr.toIntOrNull()
          ?: throw IllegalArgumentException("Invalid port '$portStr' in endpoint: $authority")
      require(parsed in 1..65535) { "Port out of range ($parsed) in endpoint: $authority" }
      return parsed
    }
  }
}

private data class HostAndPort(val host: String, val port: Int?)

private fun validateHeaders(headers: Map<String, Any>?): List<Pair<String, String>> {
  if (headers.isNullOrEmpty()) return emptyList()
  return headers.map { (rawKey, rawValue) ->
    val key = rawKey.trim().lowercase()
    require(key.isNotEmpty() && key.all { it in 'a'..'z' || it in '0'..'9' || it == '-' || it == '_' || it == '.' }) {
      "Invalid gRPC metadata header name: $rawKey"
    }
    val value = rawValue.toString()
    require(value.all { it == '\t' || it.code in 0x20..0x7e }) {
      "Invalid control character in gRPC metadata value for header '$key'"
    }
    key to value
  }
}

private fun GrpcClientConfiguration.applyHeaders(headers: List<Pair<String, String>>) {
  if (headers.isEmpty()) return

  intercept(
    object : GrpcClientInterceptor {
      override fun <Request, Response> GrpcClientCallScope<Request, Response>.intercept(
        request: Flow<Request>
      ): Flow<Response> {
        headers.forEach { (key, value) -> requestHeaders.append(key, value) }
        return proceed(request)
      }
    }
  )
}
