package xyz.mcxross.ksui.client

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.model.EventEnvelope
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.Response

class SuiWebSocketClient(override val configContainer: ConfigContainer) : SuiClient {

  /**
   * Subscribes to events that match the given filter.
   *
   * @param filter The filter to use.
   * @param callback The callback to call when an event is received.
   */
  suspend fun subscribeEvent(filter: EventFilter, callback: (EventEnvelope) -> Unit) {
    val json = Json {
      ignoreUnknownKeys = true
      isLenient = true
    }
    runBlocking {
      configContainer.httpClient.wss(
          method = HttpMethod.Post,
          host = "fullnode.devnet.sui.io",
          port = 443,
      ) {
        send(
            Frame.Text(
                buildJsonObject {
                      put("jsonrpc", "2.0")
                      put("id", "1")
                      put("method", "suix_subscribeEvent")
                      putJsonArray("params") {
                        add(json.encodeToJsonElement(serializer(), "{\"All:[]\"}"))
                      }
                    }
                    .toString()))
        while (true) {
          val event = incoming.receive() as? Frame.Text ?: continue
          val response =
              json.decodeFromString(
                  Response.serializer(EventEnvelope.serializer()), event.readText())
          if (response is Response.Ok) {
            callback(response.data)
          } else {
            throw SuiException("Error subscribing to event: ${response as Response.Error}")
          }
        }
      }
    }
    configContainer.httpClient.close()
  }
}
