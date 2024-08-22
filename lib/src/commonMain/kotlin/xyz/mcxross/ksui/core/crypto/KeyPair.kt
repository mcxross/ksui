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

/**
 * Represents a pair of signing keys: a public key and a private key.
 *
 * @property privateKey The public key in hex format.
 * @property publicKey The private key in hex format.
 */
data class KeyPair(val privateKey: ByteArray, val publicKey: ByteArray) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as KeyPair

    if (!privateKey.contentEquals(other.privateKey)) return false
    if (!publicKey.contentEquals(other.publicKey)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = privateKey.contentHashCode()
    result = 31 * result + publicKey.contentHashCode()
    return result
  }
}
