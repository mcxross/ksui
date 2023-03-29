package xyz.mcxross.ksui.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(CIO)
