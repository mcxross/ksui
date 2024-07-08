package xyz.mcxross.ksui.util

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

actual fun <T> runBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> T) {}
