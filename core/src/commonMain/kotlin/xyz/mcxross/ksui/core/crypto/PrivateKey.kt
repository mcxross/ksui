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

import org.komputing.kbech32.Bech32
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.util.SUI_PRIVATE_KEY_PREFIX
import xyz.mcxross.ksui.core.util.convertBits

interface PrivateKey : AutoCloseable {
  val data: ByteArray
  val publicKey: PublicKey

  override fun close() {}

  fun export(humanReadablePart: String = SUI_PRIVATE_KEY_PREFIX): String {
    require(humanReadablePart.length in 1..83) {
      "Human readable part must be between 1 and 83 characters"
    }

    val flag =
      when (this) {
        is Ed25519PrivateKey -> SignatureScheme.ED25519.scheme
        is Secp256k1PrivateKey -> SignatureScheme.Secp256k1.scheme
        is Secp256r1PrivateKey -> SignatureScheme.Secp256r1.scheme
        else -> throw SignatureSchemeNotSupportedException()
      }

    val keyBytes = data
    val raw = ByteArray(1 + keyBytes.size)
    raw[0] = flag
    try {
      keyBytes.copyInto(raw, destinationOffset = 1)
      return Bech32.encode(
        humanReadablePart = humanReadablePart,
        data = convertBits(raw, 8, 5, true),
      )
    } finally {
      keyBytes.fill(0)
      raw.fill(0)
    }
  }

  fun sign(data: ByteArray): Result<ByteArray, E>

  companion object {

    fun fromEncoded(encoded: String): PrivateKey = fromEncoded(encoded, SUI_PRIVATE_KEY_PREFIX)

    fun fromEncoded(encoded: String, expectedHrp: String): PrivateKey {
      val decoded = Bech32.decode(encoded)
      require(decoded.humanReadablePart.equals(expectedHrp, ignoreCase = true)) {
        "Invalid private key prefix: ${decoded.humanReadablePart}"
      }
      val convertedBit = convertBits(decoded.data, 5, 8, false)
      require(convertedBit.size == 33) {
        "Private key payload must be 32 bytes, but was ${convertedBit.size - 1}"
      }
      val schemeByte = convertedBit[0]
      val keyBytes = convertedBit.copyOfRange(1, convertedBit.size)
      convertedBit.fill(0)
      return try {
        when (schemeByte) {
          SignatureScheme.ED25519.scheme -> Ed25519PrivateKey(keyBytes)
          SignatureScheme.Secp256k1.scheme -> Secp256k1PrivateKey(keyBytes)
          SignatureScheme.Secp256r1.scheme -> Secp256r1PrivateKey(keyBytes)
          else -> throw SignatureSchemeNotSupportedException()
        }
      } finally {
        keyBytes.fill(0)
      }
    }
  }
}
