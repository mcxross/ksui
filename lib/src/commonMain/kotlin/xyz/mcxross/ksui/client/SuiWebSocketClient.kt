package xyz.mcxross.ksui.client

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.exception.EventSubscriptionException
import xyz.mcxross.ksui.model.EventEnvelope
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.EventResponse
import xyz.mcxross.ksui.model.Response
import xyz.mcxross.ksui.model.TransactionBlockEffects
import xyz.mcxross.ksui.model.TransactionFilter
import xyz.mcxross.ksui.model.TransactionSubscriptionResponse

suspend fun DefaultWebSocketSession.subscribe(method: String, params: JsonElement) {
  send(
    Frame.Text(
      buildJsonObject {
          put("jsonrpc", "2.0")
          put("id", "1")
          put("method", method)
          putJsonArray("params") { add(params) }
        }
        .toString()
    )
  )
}

suspend fun DefaultWebSocketSession.subscribeEvent(json: Json, filter: EventFilter) {
  subscribe("suix_subscribeEvent", json.encodeToJsonElement(EventFilter.serializer(), filter))
}

suspend fun DefaultWebSocketSession.subscribeTransaction(json: Json, filter: TransactionFilter) {
  subscribe(
    "suix_subscribeTransaction",
    json.encodeToJsonElement(TransactionFilter.serializer(), filter)
  )
}

class SuiWebSocketClient(override val configContainer: ConfigContainer) : SuiClient {
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  private val url = Url(whichUrl(configContainer.endPoint))

  suspend fun subscribeEvent(
    filter: EventFilter,
    onSubscribe: (Long) -> Unit = {},
    onError: (EventResponse.Error) -> Unit,
    onEvent: (EventEnvelope) -> Unit
  ) {
    val wsClient = configContainer.wsClient()
    runBlocking {
      wsClient.wss(
        method = HttpMethod.Post,
        host = url.host,
        port = if (url.port != -1) url.port else configContainer.port
      ) {
        subscribeEvent(json, filter)
        while (true) {
          val incoming = incoming.receive() as? Frame.Text ?: continue
          val response =
            json.decodeFromString<Response<EventResponse>>(serializer(), incoming.readText())
          when (response) {
            is Response.Ok -> {
              when (val data = response.data) {
                is EventResponse.Ok -> onSubscribe(data.subscriptionId)
                is EventResponse.Event -> onEvent(data.eventEnvelope)
                is EventResponse.Error -> onError(data)
              }
            }
            is Response.Error ->
              throw EventSubscriptionException(
                "Could not establish event listener: ${response.message}"
              )
          }
        }
      }
    }
    wsClient.close()
  }

  suspend fun subscribeTransaction(
    filter: TransactionFilter,
    onSubscribe: (Long) -> Unit = {},
    onError: (TransactionSubscriptionResponse.Error) -> Unit,
    onEffect: (TransactionBlockEffects) -> Unit
  ) {
    val wsClient = configContainer.wsClient()
    runBlocking {
      wsClient.wss(
        method = HttpMethod.Post,
        host = url.host,
        port = if (url.port != -1) url.port else configContainer.port
      ) {
        subscribeTransaction(json, filter)
        while (true) {
          val incoming = incoming.receive() as? Frame.Text ?: continue
          val response =
            json.decodeFromString<Response<TransactionSubscriptionResponse>>(
              serializer(),
              incoming.readText()
            )
          when (response) {
            is Response.Ok -> {
              when (val data = response.data) {
                is TransactionSubscriptionResponse.Ok -> onSubscribe(data.subscriptionId)
                is TransactionSubscriptionResponse.Effect -> {
                  onEffect(data.effect)
                }
                is TransactionSubscriptionResponse.Error -> onError(data)
              }
            }
            is Response.Error ->
              throw EventSubscriptionException(
                "Could not establish transaction listener: ${response.message}"
              )
          }
        }
      }
    }
    wsClient.close()
  }
}
