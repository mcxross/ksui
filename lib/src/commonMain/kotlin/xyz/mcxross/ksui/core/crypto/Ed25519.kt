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


class Ed25519PrivateKey(private val privateKey: ByteArray) : PrivateKey {

  override val data: ByteArray
    get() = privateKey

  override val publicKey: Ed25519PublicKey
    get() = derivePublicKey(this, SignatureScheme.ED25519) as Ed25519PublicKey
}

class Ed25519PublicKey(val data: ByteArray) : PublicKey {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Ed25519PublicKey

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return Hex(hash(Hash.BLAKE2B256, data)).toString()
  }
}
