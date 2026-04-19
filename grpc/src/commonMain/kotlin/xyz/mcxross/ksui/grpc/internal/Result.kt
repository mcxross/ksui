package xyz.mcxross.ksui.grpc.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.rpc.grpc.GrpcStatusException
import kotlinx.rpc.grpc.description
import kotlinx.rpc.grpc.status
import kotlinx.rpc.grpc.statusCode
import xyz.mcxross.ksui.core.exception.GraphQLError
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result

internal suspend fun <T> handleGrpc(call: suspend () -> T): Result<T, SuiError> =
  try {
    Result.Ok(call())
  } catch (e: CancellationException) {
    throw e
  } catch (e: GrpcStatusException) {
    Result.Err(e.toSuiError())
  } catch (e: Exception) {
    Result.Err(e.toSuiError())
  }

internal fun <T> Flow<T>.asGrpcResult(): Flow<Result<T, SuiError>> =
  map<T, Result<T, SuiError>> { Result.Ok(it) }
    .catch { e ->
      if (e is CancellationException) throw e
      if (e !is Exception) throw e
      emit(Result.Err(e.toSuiError()))
    }

private fun Exception.toSuiError(): SuiError =
  when (this) {
    is GrpcStatusException -> {
      val code = status.statusCode
      val description = status.description
      SuiError(
        listOf(
          GraphQLError(
            message = description ?: code.name,
            extensions =
              mapOf(
                "code" to code.name,
                "grpcStatus" to code.value,
                "exception" to "GrpcStatusException",
              ),
          )
        )
      )
    }
    else ->
      SuiError(
        listOf(
          GraphQLError(
            message = "gRPC client request failed: ${message ?: "unknown error"}",
            extensions = mapOf("exception" to "Throwable"),
          )
        )
      )
  }
