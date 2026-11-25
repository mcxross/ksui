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
package xyz.mcxross.ksui.core

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.core.exception.ParsingException

enum class HexInvalidReason(val reason: String) {
  TOO_SHORT("Too Short"),
  INVALID_LENGTH("Invalid Length"),
  INVALID_HEX_CHARS("Invalid Hex Chars"),
}

@Serializable
class Hex() {
  private var data: ByteArray = byteArrayOf()

  constructor(data: ByteArray) : this() {
    this.data = data.copyOf()
  }

  fun toByteArray(): ByteArray = data.copyOf()

  fun toStringWithoutPrefix(): String =
    data.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }

  override fun toString(): String = "0x${toStringWithoutPrefix()}"

  companion object {

    fun empty(): Hex = Hex()

    fun fromString(hex: String): Hex {
      val hexString = hex.removePrefix("0x").removePrefix("0X")
      if (hexString.length % 2 != 0) {
        throw ParsingException(HexInvalidReason.INVALID_LENGTH.reason)
      }
      val out = ByteArray(hexString.length / 2)
      for (i in out.indices) {
        val hi = hexDigit(hexString[i * 2])
        val lo = hexDigit(hexString[i * 2 + 1])
        out[i] = ((hi shl 4) or lo).toByte()
      }
      return Hex(out)
    }

    fun fromHexInput(byteArray: ByteArray): Hex = Hex(byteArray)

    private fun hexDigit(c: Char): Int =
      when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> throw ParsingException(HexInvalidReason.INVALID_HEX_CHARS.reason)
      }
  }
}
