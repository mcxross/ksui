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
import xyz.mcxross.ksui.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.util.SUI_PRIVATE_KEY_PREFIX
import xyz.mcxross.ksui.util.convertBits

/**
 * This interface defines the `PrivateKey` interface, which represents a private key in the SUI
 * blockchain. The private key is used to sign transactions and messages.
 *
 * The `[PrivateKey]` interface also implements SIP-15 for its private key import and export
 * methods. This is to visually distinguish a 32-byte private key representation from a 32-bytes Sui
 * address that is currently also Hex encoded
 */
interface PrivateKey {
  val data: ByteArray
  val publicKey: PublicKey

  /**
   * Encodes the private key using Bech32 with the specified human-readable part.
   *
   * The default human-readable part is [SUI_PRIVATE_KEY_PREFIX].
   *
   * @param humanReadablePart The human-readable part to use in the Bech32 encoding. Defaults to
   *   [SUI_PRIVATE_KEY_PREFIX].
   * @return The Bech32 encoded private key.
   */
  fun export(humanReadablePart: String = SUI_PRIVATE_KEY_PREFIX): String {
    require(humanReadablePart.length in 1..83) {
      "Human readable part must be between 1 and 83 characters"
    }

    val flag =
      when (this) {
        is Ed25519PrivateKey -> SignatureScheme.ED25519.scheme
        else -> throw SignatureSchemeNotSupportedException()
      }

    return Bech32.encode(
      humanReadablePart = humanReadablePart,
      data = convertBits(byteArrayOf(flag) + data, 8, 5, true),
    )
  }

  companion object {

    /**
     * Creates a PrivateKey instance from a Bech32 encoded string.
     *
     * @param encoded The Bech32 encoded private key.
     * @return The PrivateKey instance.
     * @throws SignatureSchemeNotSupportedException If the signature scheme is not supported.
     */
    fun fromEncoded(encoded: String): PrivateKey {
      val convertedBit = convertBits(Bech32.decode(encoded).data, 5, 8, false)
      return when (convertedBit[0]) {
        SignatureScheme.ED25519.scheme ->
          Ed25519PrivateKey(convertedBit.sliceArray(1 until convertedBit.size))
        else -> throw SignatureSchemeNotSupportedException()
      }
    }
  }
}
