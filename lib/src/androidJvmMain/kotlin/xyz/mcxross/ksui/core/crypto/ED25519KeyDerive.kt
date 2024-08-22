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

import com.google.common.primitives.Bytes
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDPath
import org.bitcoinj.crypto.HDUtils

class ED25519KeyDerive(val key: ByteArray, val chaincode: ByteArray) {

  fun derive(index: Int): ED25519KeyDerive {
    if (!hasHardenedBit(index)) {
      // todo: create an exception
      throw RuntimeException()
    }

    val indexBytes = ByteArray(4)
    ByteBuffer.wrap(indexBytes).putInt(index)

    val data = Bytes.concat(byteArrayOf(0x00), this.key, indexBytes)

    val i = HDUtils.hmacSha512(this.chaincode, data)
    val il = Arrays.copyOfRange(i, 0, 32)
    val ir = Arrays.copyOfRange(i, 32, 64)

    return ED25519KeyDerive(il, ir)
  }

  fun deriveFromPath(path: String = DEFAULT_DERIVE_PATH): ED25519KeyDerive {
    require(path.isNotBlank()) { "Path cannot be blank" }
    val hdPath = HDPath.parsePath(path)
    val it: Iterator<ChildNumber> = hdPath.iterator()
    var current = this
    while (it.hasNext()) {
      current = current.derive(it.next().i)
    }
    return current
  }

  private fun hasHardenedBit(a: Int): Boolean {
    return (a and ChildNumber.HARDENED_BIT) != 0
  }

  companion object {
    private const val DEFAULT_DERIVE_PATH = "m/44H/784H/0H/0H/0H"

    fun createKeyByDefaultPath(seed: ByteArray): ED25519KeyDerive {
      return createMasterKey(seed).deriveFromPath()
    }

    fun createMasterKey(seed: ByteArray): ED25519KeyDerive {
      val i = HDUtils.hmacSha512("ed25519 seed".toByteArray(Charset.defaultCharset()), seed)
      val il = Arrays.copyOfRange(i, 0, 32)
      val ir = Arrays.copyOfRange(i, 32, 64)
      return ED25519KeyDerive(il, ir)
    }
  }
}
