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

fun formatSuiDomain(domain: String): String {
  val lowerCase = domain.lowercase()
  val parts = lowerCase.split(".")
  return if (parts.size == 1) {
    parts.first() + ".sui"
  } else {
    lowerCase
  }
}

fun idToMapped(id: String): Map<String, String> {
  val parts = id.split("::")
  if (parts.size != 3) {
    throw IllegalArgumentException(
      "Invalid ID. Expected 3 parts of the form 'packageId::module::function', but got ${parts.size} parts."
    )
  }
  return mapOf(PACKAGE_ID to parts[0], MODULE to parts[1], FUNCTION to parts[2])
}

fun idToParts(id: String): Triple<String, String, String> {
  val parts = id.split("::")
  if (parts.size != 3) {
    throw IllegalArgumentException(
      "Invalid ID. Expected 3 parts of the form 'packageId::module::function', but got ${parts.size} parts."
    )
  }
  return Triple(parts[0], parts[1], parts[2])
}

fun convertBits(data: ByteArray, fromBits: Int, toBits: Int, pad: Boolean): ByteArray {
  var acc = 0
  var bits = 0
  val maxv = (1 shl toBits) - 1
  val result = mutableListOf<Byte>()

  for (value in data) {
    acc = (acc shl fromBits) or (value.toInt() and 0xff)
    bits += fromBits
    while (bits >= toBits) {
      bits -= toBits
      result.add(((acc shr bits) and maxv).toByte())
    }
  }

  if (pad && bits > 0) {
    result.add(((acc shl (toBits - bits)) and maxv).toByte())
  } else if (bits >= fromBits || ((acc shl (toBits - bits)) and maxv) != 0) {
    throw IllegalArgumentException("Invalid bits!")
  }

  return result.toByteArray()
}
