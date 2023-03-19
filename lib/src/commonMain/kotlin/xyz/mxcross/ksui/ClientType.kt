package xyz.mxcross.ksui

/**
 * An enumeration of the different types of clients that can be created by [ClientConfig.build].
 *
 * The available types are:
 * - [HTTP]: Represents an HTTP client.
 * - [WS]: Represents a WebSocket client.
 */
enum class ClientType {
  HTTP,
  WS
}
