package xyz.mcxross.ksui.util

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.client.EndPoint
import xyz.mcxross.ksui.client.SuiHttpClient
import xyz.mcxross.ksui.exception.TooManyRequestsException
import xyz.mcxross.ksui.model.FaucetResponse
import xyz.mcxross.ksui.model.SuiAddress

@Throws(TooManyRequestsException::class, CancellationException::class)
suspend fun SuiHttpClient.requestTestTokens(owner: SuiAddress): FaucetResponse {
  val response =
      configContainer.httpClient.post {
        url(
            when (configContainer.endPoint) {
              EndPoint.DEVNET -> "https://faucet.devnet.sui.io/gas"
              EndPoint.TESTNET -> "https://faucet.testnet.sui.io/gas"
              else -> {
                configContainer.customUrl
              }
            })

        contentType(ContentType.Application.Json)
        setBody(
            buildJsonObject {
                  putJsonObject("FixedAmountRequest") { put("recipient", owner.pubKey) }
                }
                .toString())
      }

  if (response.status.isSuccess()) {
    when (val result = json.decodeFromString<FaucetResponse>(serializer(), response.bodyAsText())) {
      is FaucetResponse.Ok -> return result
      is FaucetResponse.Error -> throw Exception(result.message)
    }
  } else if (response.status.value == 429) {
    throw TooManyRequestsException(429, "Too many requests")
  } else {
    throw Exception("Unknown error")
  }
}
