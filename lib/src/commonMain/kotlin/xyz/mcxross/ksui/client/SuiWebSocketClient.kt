package xyz.mcxross.ksui.client

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.exception.EventSubscriptionException
import xyz.mcxross.ksui.model.EventEnvelope
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.EventResponse
import xyz.mcxross.ksui.model.Response

suspend fun DefaultWebSocketSession.subscribe(filter: EventFilter) {
  send(
      Frame.Text(
          buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", "1")
                put("method", "suix_subscribeEvent")
                putJsonArray("params") {
                  addJsonObject {
                    put(
                        "Package",
                        "0xc74531639fadfb02d30f05f37de4cf1e1149ed8d23658edd089004830068180b")
                  }
                }
              }
              .toString()))
}

class SuiWebSocketClient(override val configContainer: ConfigContainer) : SuiClient {

  val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  private val url = Url(whichUrl(configContainer.endPoint))

  /**
   * Subscribes to events that match the given filter.
   *
   * @param filter The filter to use.
   * @param callback The callback to call when an event is received.
   */
  suspend fun subscribeEvent(
      filter: EventFilter,
      onSubscribe: (Long) -> Unit = {},
      onError: (EventResponse.Error) -> Unit,
      onEvent: (EventEnvelope) -> Unit
  ) {
    runBlocking {
      configContainer.httpClient.wss(
          method = HttpMethod.Post,
          host = url.host,
          port = if (url.port != -1) url.port else configContainer.port) {
            subscribe(filter)
            while (true) {
              val incoming = incoming.receive() as? Frame.Text ?: continue
              val response =
                  json.decodeFromString<Response<EventResponse>>(serializer(), incoming.readText())
              when (response) {
                is Response.Ok -> {
                  when (response.data) {
                    is EventResponse.Ok -> onSubscribe(response.data.subscriptionId)
                    is EventResponse.Event -> onEvent(response.data.eventEnvelope)
                    is EventResponse.Error -> onError(response.data)
                  }
                }
                is Response.Error ->
                    throw EventSubscriptionException("Could not establish event listener")
              }
            }
          }
    }
    configContainer.httpClient.close()
  }
}
