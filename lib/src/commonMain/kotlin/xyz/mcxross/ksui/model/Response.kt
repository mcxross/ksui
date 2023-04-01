package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.ResponseSerializer

/**
 * A sealed class representing the response of an API call.
 *
 * The response can either be an [Ok] response containing a [Ok.data] of type [T], or an [Error]
 * response
 *
 * containing an HTTP [Error.code] and an error [Error.message].
 */
@Serializable(with = ResponseSerializer::class)
sealed class Response<out T> {

  /** Represents an [Ok] response containing a [data] of type [T]. */
  data class Ok<out T>(val data: T) : Response<T>()
  /** Represents an [Error] response containing an HTTP [code] and an error [message]. */
  data class Error(
    val code: Int,
    val message: String,
  ) : Response<Nothing>()
}
