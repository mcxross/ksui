package xyz.mcxross.ksui

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*

fun mockEngine(response: String): MockEngine = MockEngine {
  respond(
    content = ByteReadChannel(response),
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
  )
}
