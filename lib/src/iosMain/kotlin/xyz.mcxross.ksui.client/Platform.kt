package xyz.mcxross.ksui.client

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

actual val defaultEngine: HttpClientEngine = Darwin.create()

actual suspend fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.runBlocking(context, block)
