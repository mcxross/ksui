package xyz.mcxross.ksui.client

import io.ktor.client.engine.*
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

actual val defaultEngine: HttpClientEngine
  get() = TODO("Not yet implemented")

actual suspend fun <T> runBlocking(
  context: CoroutineContext,
  block: suspend CoroutineScope.() -> T
): T {
  TODO("Not yet implemented")
}
