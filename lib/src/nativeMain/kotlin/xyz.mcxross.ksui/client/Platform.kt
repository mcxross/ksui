package xyz.mcxross.ksui.client

import io.ktor.client.*

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(config)
