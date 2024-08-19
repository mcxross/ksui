/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
