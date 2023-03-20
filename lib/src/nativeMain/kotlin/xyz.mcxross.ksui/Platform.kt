package xyz.mcxross.ksui

import io.ktor.client.*

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(config)
