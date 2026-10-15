@file:OptIn(ExperimentalObjCName::class)

package xyz.mcxross.ksui.core.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

// Swift already has `Result`, which this would shadow in files that import SuiKit.
@ObjCName(swiftName = "SuiResult")
sealed class Result<out V, out E> {
  data class Ok<out V>(val value: V) : Result<V, Nothing>()

  data class Err<out E>(val error: E) : Result<Nothing, E>()

  fun expect(message: String): V =
    when (this) {
      is Ok -> value
      is Err -> throw IllegalStateException("$message: $error")
    }

  inline fun expect(message: () -> String): V =
    when (this) {
      is Ok -> value
      is Err -> throw IllegalStateException("${message()}: $error")
    }

  inline fun expectErr(message: () -> String): E =
    when (this) {
      is Ok -> throw IllegalStateException("${message()}: $value")
      is Err -> error
    }

  fun unwrap(): V =
    when (this) {
      is Ok -> value
      is Err -> throw IllegalStateException("called unwrap on an Err: $error")
    }

  fun unwrapErr(): E =
    when (this) {
      is Ok -> throw IllegalStateException("called unwrapErr on an Ok: $value")
      is Err -> error
    }
}
