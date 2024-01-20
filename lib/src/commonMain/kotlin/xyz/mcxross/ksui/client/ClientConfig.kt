package xyz.mcxross.ksui.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import xyz.mcxross.ksui.util.getKtorLogLevel
import xyz.mcxross.ksui.util.getKtorLogger

data class ConfigContainer(
  val engine: HttpClientEngine? = null,
  val endPoint: EndPoint,
  val customUrl: String,
  val port: Int = 443,
  val maxRetries: Int,
  val agentName: String,
  val requestTimeout: Long,
  val connectionTimeout: Long,
  val enableLogging: Boolean,
  val loggerWrapper: xyz.mcxross.ksui.util.Logger,
  val logLevelWrapper: xyz.mcxross.ksui.util.LogLevel,
) {

  private val selectedEngine = engine ?: defaultEngine

  fun httpClient() =
    HttpClient(selectedEngine) {
      install(UserAgent) { agent = agentName }
      install(ContentNegotiation) { json() }
      install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = maxRetries)
        exponentialDelay()
      }
      install(HttpTimeout) {
        requestTimeoutMillis = requestTimeout
        connectTimeoutMillis = connectionTimeout
      }
      if (enableLogging)
        install(Logging) {
          logger = loggerWrapper.getKtorLogger()
          level = logLevelWrapper.getKtorLogLevel()
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
   * Enables logging of requests and responses.
   *
   * Defaults to false
   */
  var enableLogging: Boolean = false

  /**
   * Sets the logger to use.
   *
   * Defaults to [Logger.SIMPLE]
   */
  var logger: xyz.mcxross.ksui.util.Logger = xyz.mcxross.ksui.util.Logger.SIMPLE

  /**
   * Sets the log level to use.
   *
   * Defaults to [LogLevel.INFO]
   */
  var logLevel: xyz.mcxross.ksui.util.LogLevel = xyz.mcxross.ksui.util.LogLevel.INFO

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
            enableLogging = enableLogging,
            loggerWrapper = logger,
            logLevelWrapper = logLevel,
          )
        )
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
            enableLogging = enableLogging,
            loggerWrapper = logger,
            logLevelWrapper = logLevel,
          )
        )
    }
}
