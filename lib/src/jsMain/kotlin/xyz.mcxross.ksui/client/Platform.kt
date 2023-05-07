package xyz.mcxross.ksui.client

import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Js)

@OptIn(DelicateCoroutinesApi::class)
actual suspend fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = suspendCoroutine { continuation ->
  GlobalScope.launch(Dispatchers.Default) {
    try {
      val result = block()
      continuation.resume(result)
    } catch (exception: Throwable) {
      continuation.resumeWithException(exception)
    }
  }
}
