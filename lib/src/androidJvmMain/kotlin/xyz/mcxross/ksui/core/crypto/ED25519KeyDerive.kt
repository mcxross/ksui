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
package xyz.mcxross.ksui.core.crypto

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.Arrays
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ED25519KeyDerive(val key: ByteArray, val chaincode: ByteArray) {

  fun derive(index: Int): ED25519KeyDerive {
    if (!hasHardenedBit(index)) {
      // todo: create an exception
      throw RuntimeException()
    }

    val indexBytes = ByteArray(4)
    ByteBuffer.wrap(indexBytes).putInt(index)

    val data = ByteArray(1 + this.key.size + indexBytes.size)
    data[0] = 0x00
    this.key.copyInto(data, destinationOffset = 1)
    indexBytes.copyInto(data, destinationOffset = 1 + this.key.size)

    val i = hmacSha512(this.chaincode, data)
    val il = Arrays.copyOfRange(i, 0, 32)
    val ir = Arrays.copyOfRange(i, 32, 64)

    return ED25519KeyDerive(il, ir)
  }

  fun deriveFromPath(path: String = DEFAULT_DERIVE_PATH): ED25519KeyDerive {
    require(path.isNotBlank()) { "Path cannot be blank" }
    var current = this
    for (index in parsePath(path)) {
      current = current.derive(index)
    }
    return current
  }

  private fun hasHardenedBit(a: Int): Boolean {
    return (a and HARDENED_BIT) != 0
  }

  companion object {
    private const val DEFAULT_DERIVE_PATH = "m/44H/784H/0H/0H/0H"
    private const val HARDENED_BIT = -0x80000000

    fun createKeyByDefaultPath(seed: ByteArray): ED25519KeyDerive {
      return createMasterKey(seed).deriveFromPath()
    }

    fun createMasterKey(seed: ByteArray): ED25519KeyDerive {
      val i = hmacSha512("ed25519 seed".toByteArray(Charset.defaultCharset()), seed)
      val il = Arrays.copyOfRange(i, 0, 32)
      val ir = Arrays.copyOfRange(i, 32, 64)
      return ED25519KeyDerive(il, ir)
    }

    private fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
      val mac = Mac.getInstance("HmacSHA512")
      mac.init(SecretKeySpec(key, "HmacSHA512"))
      return mac.doFinal(data)
    }

    private fun parsePath(path: String): List<Int> {
      val parts = path.split("/")
      require(parts.firstOrNull() == "m") { "Path must start with m" }
      return parts.drop(1).map { child ->
        require(child.isNotBlank()) { "Path contains an empty child index" }
        val hardened = child.endsWith("H") || child.endsWith("'")
        require(hardened) { "Only hardened derivation is supported" }
        val value = child.dropLast(1).toLongOrNull()
        require(value != null && value in 0..Int.MAX_VALUE.toLong()) {
          "Invalid child index: $child"
        }
        value.toInt() or HARDENED_BIT
      }
    }
  }
}
