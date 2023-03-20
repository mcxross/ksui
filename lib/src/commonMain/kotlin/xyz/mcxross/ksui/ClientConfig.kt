package xyz.mcxross.ksui

import io.ktor.client.*
import io.ktor.client.plugins.*

data class ConfigContainer(
  val httpClient: HttpClient,
  val endPoint: EndPoint,
  val customUrl: String,
  val maxRetries: Int,
  val agentName: String,
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
            },
            endpoint,
            customEndPointUrl,
            maxRetries,
            agentName
          )
        )
      }
      ClientType.WS -> SuiWebSocketClient()
    }
}
