package xyz.mcxross.ksui.core.client

import xyz.mcxross.ksui.core.model.UserAgent

class ClientConfig(
  var pipelining: Boolean = false,
  var pipelineMaxSize: Int = 20,
  var followRedirects: Boolean = true,
  var followSslRedirects: Boolean = true,
  var maxConnectionsPerRoute: Int = 100,
  var maxConnectionsCount: Int = 1000,
  var connectTimeoutMillis: Long = 10000L,
  var readTimeoutMillis: Long = 10000L,
  var writeTimeoutMillis: Long = 10000L,
  var keepAliveTime: Long = 5000L,
  var connectAttempts: Int = 5,
  var retryOnServerErrors: Int = -1,
  var maxRetries: Int = -1,
  var cache: Boolean = false,
  var agent: String = "Ksui",
  var likeAgent: UserAgent? = null,
  var requestTimeout: Long = 10000L,
  var connectTimeout: Long = 10000L,
  var proxy: String? = null,
) {
  companion object {
    val default: ClientConfig
      get() = ClientConfig()
  }
}
