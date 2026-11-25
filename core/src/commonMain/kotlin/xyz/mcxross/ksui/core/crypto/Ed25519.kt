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

import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.Result

class Ed25519PrivateKey(privateKey: ByteArray) : PrivateKey {

  private val keyBytes: ByteArray =
    privateKey.copyOf().also {
      require(it.size == 32) { "Ed25519 private key must be 32 bytes, but was ${it.size}" }
    }
  private var closed: Boolean = false

  override val data: ByteArray
    get() {
      check(!closed) { "Private key is closed" }
      return keyBytes.copyOf()
    }

  override val publicKey: Ed25519PublicKey by lazy {
    derivePublicKey(this, SignatureScheme.ED25519) as Ed25519PublicKey
  }

  constructor(
    scheme: SignatureScheme = SignatureScheme.ED25519
  ) : this(generateEd25519KeyBytes(scheme))

  constructor(privateKey: String) : this(decodeEd25519KeyBytes(privateKey))

  override fun sign(data: ByteArray): Result<ByteArray, E> {
    if (closed) return Result.Err(IllegalStateException("Private key is closed"))
    return sign(data, this)
  }

  override fun close() {
    keyBytes.fill(0)
    closed = true
  }

  companion object {
    private fun generateEd25519KeyBytes(scheme: SignatureScheme): ByteArray {
      require(scheme == SignatureScheme.ED25519) {
        "Unsupported scheme for Ed25519PrivateKey: $scheme"
      }
      return randomEd25519SecretKey()
    }

    private fun decodeEd25519KeyBytes(encoded: String): ByteArray {
      val parsed = PrivateKey.fromEncoded(encoded)
      try {
        require(parsed is Ed25519PrivateKey) {
          "Expected Ed25519 private key, but got ${parsed.publicKey.scheme()}"
        }
        return parsed.data
      } finally {
        parsed.close()
      }
    }
  }
}

class Ed25519PublicKey(data: ByteArray) : PublicKey {
  private val keyBytes: ByteArray = data.copyOf()

  override val data: ByteArray
    get() = keyBytes.copyOf()

  override fun scheme(): SignatureScheme = SignatureScheme.ED25519

  override fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    verifySignature(this, message, signature)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as Ed25519PublicKey
    return keyBytes.contentEquals(other.keyBytes)
  }

  override fun hashCode(): Int = keyBytes.contentHashCode()

  // Not an address: Sui addresses are blake2b(flag || key). Use AccountAddress.fromPublicKey.
  override fun toString(): String = "Ed25519PublicKey(${Hex(keyBytes)})"
}
