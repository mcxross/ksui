package xyz.mcxross.ksui.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*

data class ConfigContainer(
    val engine: HttpClientEngine? = null,
    val endPoint: EndPoint,
    val customUrl: String,
    val port: Int = 443,
    val maxRetries: Int,
    val agentName: String,
    val requestTimeout: Long,
    val connectionTimeout: Long,
) {

  private val selectedEngine = engine ?: defaultEngine
  fun httpClient() =
      HttpClient(selectedEngine) {
        install(UserAgent) { agent = agentName }
        install(HttpRequestRetry) {
          retryOnServerErrors(maxRetries = maxRetries)
          exponentialDelay()
        }
        install(HttpTimeout) {
          requestTimeoutMillis = requestTimeout
          connectTimeoutMillis = connectionTimeout
        }
      }

  fun wsClient() = HttpClient {
    install(UserAgent) { agent = agentName }
    install(WebSockets)
  }
}

class ClientConfig {

  var engine: HttpClientEngine? = null

  /**
   * Sets the [EndPoint] to make calls to.
   *
   * If [EndPoint.CUSTOM] is set, you must explicitly provide the url by calling [customEndPointUrl]
   */
  var endpoint: EndPoint = EndPoint.DEVNET

  /**
   * Sets a custom [EndPoint] to make calls to.
   *
   * The value set by this will only be considered *IF* [EndPoint.CUSTOM] is set, it'll be ignored
   * otherwise
   */
  var customEndPointUrl = ""

  /**
   * Sets the max number of retries.
   *
   * Defaults to 5
   */
  var maxRetries: Int = 5

  /**
   * Sets the agent name.
   *
   * Defaults to KSUI/VERSION-#.#.#
   */
  var agentName: String = "KSUI/$KSUI_VERSION"

  /**
   * Sets the request timeout in milliseconds.
   *
   * Defaults to 30 seconds
   */
  var requestTimeout: Long = 30_000

  /**
   * Sets the connection timeout in milliseconds.
   *
   * Defaults to 30 seconds
   */
  var connectionTimeout: Long = 30_000

  /**
   * Builds a new instance of [SuiHttpClient].
   *
   * @return [SuiHttpClient]
   */
  fun build(clientType: ClientType): SuiClient =
      when (clientType) {
        ClientType.HTTP -> {
          SuiHttpClient(
              ConfigContainer(
                  engine = engine,
                  endPoint = endpoint,
                  customUrl = customEndPointUrl,
                  maxRetries = maxRetries,
                  agentName = agentName,
                  requestTimeout = requestTimeout,
                  connectionTimeout = connectionTimeout,
              ))
        }
        ClientType.WS ->
            SuiWebSocketClient(
                ConfigContainer(
                    engine = engine,
                    endPoint = endpoint,
                    customUrl = customEndPointUrl,
                    maxRetries = maxRetries,
                    agentName = agentName,
                    requestTimeout = requestTimeout,
                    connectionTimeout = connectionTimeout,
                ))
      }
}
