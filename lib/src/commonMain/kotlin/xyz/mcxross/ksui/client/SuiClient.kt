package xyz.mcxross.ksui.client

/**
 * An interface representing a Sui client.
 *
 * A Sui client is responsible for sending requests to a Sui end point and receiving responses. It
 * is implemented by the [SuiHttpClient] and [SuiWebSocketClient] classes.
 *
 * Example usage:
 * ```
 * // Create a Sui HTTP client
 * val client = createSuiHttpClient {
 *     setEndPoint(EndPoint.DEVNET)
 * }
 * ```
 */
interface SuiClient {

  val configContainer: ConfigContainer
  /**
   * Returns the current Sui end point.
   *
   * @return The current Sui end point.
   */
  fun whichUrl(endPoint: EndPoint): String {
    return when (endPoint) {
      EndPoint.CUSTOM -> {
        configContainer.customUrl
      }
      EndPoint.DEVNET -> {
        "https://fullnode.devnet.sui.io:443"
      }
      EndPoint.TESTNET -> {
        "https://fullnode.testnet.sui.io:443"
      }
      EndPoint.MAINNET -> {
        "https://fullnode.mainnet.sui.io:443"
      }
    }
  }
}
