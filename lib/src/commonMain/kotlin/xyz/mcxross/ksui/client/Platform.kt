package xyz.mcxross.ksui.client

import io.ktor.client.engine.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope

expect val defaultEngine: HttpClientEngine

expect suspend fun <T> runBlocking(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> T
): T
