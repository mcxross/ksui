package xyz.mcxross.ksui.client

import io.ktor.client.engine.*
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

actual val defaultEngine: HttpClientEngine = TODO()

actual suspend fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.runBlocking(context, block)
