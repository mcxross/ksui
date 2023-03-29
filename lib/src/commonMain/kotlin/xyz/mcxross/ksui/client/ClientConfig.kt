package xyz.mcxross.ksui.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import xyz.mcxross.ksui.model.EndPoint
import xyz.mcxross.ksui.model.SuiWebSocketClient

data class ConfigContainer(
    val httpClient: HttpClient,
    val endPoint: EndPoint,
    val customUrl: String,
    val maxRetries: Int,
    val agentName: String,
    val requestTimeout: Long,
    val connectionTimeout: Long,
)

class ClientConfig {

  var engine: HttpClientConfig<*>.() -> Unit = {}

  /**
   * Sets the [EndPoint] to make calls to.
   *
   * If [EndPoint.CUSTOM] is set, you must explicitly provide the url by calling [setCustomEndPoint]
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
            httpClient {
              install(UserAgent) { agent = agentName }
              install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = maxRetries)
                exponentialDelay()
              }
              install(HttpTimeout) {
                requestTimeoutMillis = requestTimeout
                connectTimeoutMillis = connectionTimeout
              }
            },
            endpoint,
            customEndPointUrl,
            maxRetries,
            agentName,
            requestTimeout,
            connectionTimeout,
          )
        )
      }
      ClientType.WS -> SuiWebSocketClient()
    }
}
