package xyz.mcxross.ksui.client

/**
 * Creates a new instance of [SuiHttpClient] using the provided configuration.
 *
 * The configuration for the client is defined by a lambda function that takes an instance of
 * [ClientConfig] as its receiver and modifies its properties as needed. The lambda function is
 * passed to the function as a parameter of type `Config.() -> Unit`.
 *
 * Example usage:
 * ```
 * val client = createSuiHttpClient { setEndPoint(EndPoint.DEVNET) }
 * ```
 *
 * @param builderAction A lambda function that modifies the properties of a [ClientConfig] instance.
 * @return An instance of [SuiHttpClient] configured according to the provided [ClientConfig]
 *   instance.
 */
fun createSuiHttpClient(builderAction: ClientConfig.() -> Unit): SuiHttpClient {
  val suiRpcClient = ClientConfig()
  suiRpcClient.builderAction()
  return suiRpcClient.build(ClientType.HTTP) as SuiHttpClient
}

/**
 * Creates a new instance of [SuiWebSocketClient] using the provided configuration.
 *
 * The configuration for the client is defined by a lambda function that takes an instance of
 * [ClientConfig] as its receiver and modifies its properties as needed. The lambda function is
 * passed to the function as a parameter of type `Config.() -> Unit`.
 *
 * Example usage:
 * ```
 *
 *
 *
 * ```
 *
 * @param builderAction A lambda function that modifies the properties of a [ClientConfig] instance.
 * @return An instance of [SuiWebSocketClient] configured according to the provided [ClientConfig]
 *   instance.
 */
fun createSuiWebSocketClient(builderAction: ClientConfig.() -> Unit): SuiWebSocketClient {
  val clientConfig = ClientConfig()
  clientConfig.builderAction()
  return clientConfig.build(ClientType.WS) as SuiWebSocketClient
}
