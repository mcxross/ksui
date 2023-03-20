package xyz.mcxross.ksui

import io.ktor.client.*

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
