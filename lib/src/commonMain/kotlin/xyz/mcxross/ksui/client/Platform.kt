package xyz.mcxross.ksui.client

import io.ktor.client.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

expect suspend fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T
