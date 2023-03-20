package xyz.mcxross.ksui

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
interface SuiClient
