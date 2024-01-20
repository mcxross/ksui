package xyz.mcxross.ksui.util

import io.ktor.client.plugins.logging.*

enum class Logger {
  SIMPLE,
  EMPTY,
  DEFAULT,
}

fun Logger.getKtorLogger(): io.ktor.client.plugins.logging.Logger {
  return when (this) {
    Logger.SIMPLE -> io.ktor.client.plugins.logging.Logger.SIMPLE
    Logger.EMPTY -> io.ktor.client.plugins.logging.Logger.EMPTY
    Logger.DEFAULT -> io.ktor.client.plugins.logging.Logger.DEFAULT
  }
}

enum class LogLevel {
  INFO,
  ALL,
  BODY,
  NONE,
  HEADERS,
}

fun LogLevel.getKtorLogLevel(): io.ktor.client.plugins.logging.LogLevel {
  return when (this) {
    LogLevel.INFO -> io.ktor.client.plugins.logging.LogLevel.INFO
    LogLevel.ALL -> io.ktor.client.plugins.logging.LogLevel.ALL
    LogLevel.BODY -> io.ktor.client.plugins.logging.LogLevel.BODY
    LogLevel.NONE -> io.ktor.client.plugins.logging.LogLevel.NONE
    LogLevel.HEADERS -> io.ktor.client.plugins.logging.LogLevel.HEADERS
  }
}
