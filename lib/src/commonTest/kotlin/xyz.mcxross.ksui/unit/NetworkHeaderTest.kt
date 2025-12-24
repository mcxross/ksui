package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import xyz.mcxross.ksui.model.*
import xyz.mcxross.ksui.util.runBlocking

class NetworkHeaderTest :
  StringSpec({
    "Ktor request should include custom headers from SuiConfig" {
      val customSettings =
        SuiSettings(
          network = Network.CUSTOM,
          fullNode = "https://custom-fullnode.com",
          fullNodeConfig = FullNodeConfig(headers = mapOf("X-Custom-Header" to "CustomValue")),
        )
      val config = SuiConfig(customSettings)

      val mockEngine = MockEngine { request ->
        request.headers["X-Custom-Header"] shouldBe "CustomValue"
        respond(
          content = "{}",
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }

      // We need to bypass the real httpClient(config) to use our mock engine
      // In a real scenario, we might want to inject the engine or client
      val client =
        io.ktor.client.HttpClient(mockEngine) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

      runBlocking {
        client.post(config.getRequestUrl(SuiApiType.FULLNODE)) {
          contentType(ContentType.Application.Json)
          config.getHeaders(SuiApiType.FULLNODE)?.forEach { (key, value) -> header(key, value) }
          setBody("{}")
        }
      }
    }
  })
