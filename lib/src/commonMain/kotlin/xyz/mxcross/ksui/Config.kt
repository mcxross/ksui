package xyz.mxcross.ksui

import io.ktor.client.*
import io.ktor.client.engine.cio.*

data class ConfigContainer(
  val httpClient: HttpClient,
  val endPoint: EndPoint,
  val customUrl: String,
  val retries: Int,
  val agentName: String,
)

class Config {

  private var httpClient = HttpClient(CIO)

  private var endpoint: EndPoint = EndPoint.DEVNET

  private var customUrl = ""

  private var retries: Int = 5

  private var agentName: String = "KSUI/$KSUI_VERSION"

  /**
   * Sets the [HttpClient] to use for making HTTP requests, calls.
   *
   * @param httpClient for making HTTP requests. Defaults to environments best.
   * @return [Unit]
   */
  fun setClient(httpClient: HttpClient) = apply { this.httpClient = httpClient }

  /**
   * Sets the [EndPoint] to make calls to.
   *
   * If [EndPoint.CUSTOM] is set, you must explicitly provide the url by calling [setCustomEndPoint]
   *
   * @param endPoint [EndPoint]
   * @return [Unit]
   */
  fun setEndPoint(endPoint: EndPoint) = apply { this.endpoint = endPoint }

  /**
   * Sets a custom [EndPoint] to make calls to.
   *
   * The value set by this will only be considered *IF* [EndPoint.CUSTOM] is set, it'll be ignored
   * otherwise
   *
   * @param url to the [EndPoint]
   * @return [Unit]
   */
  fun setCustomEndPoint(url: String) = apply { this.customUrl = url }

  /**
   * Sets the number of retries.
   *
   * Defaults to 5
   *
   * @param number of retries
   * @return [Unit]
   */
  fun setRetries(number: Int) = apply { this.retries = number }

  /**
   * Sets the agent name.
   *
   * Defaults to KSUI/VERSION-#
   *
   * @param name of the Agent
   * @return [Unit]
   */
  fun setAgentName(name: String) = apply { this.agentName = name }

  /**
   * Builds a new instance of [SuiRpcClient].
   *
   * @return [SuiRpcClient]
   */
  fun build() = SuiRpcClient(ConfigContainer(httpClient, endpoint, customUrl, retries, agentName))
}
