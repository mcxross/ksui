/*
 * Copyright 2025 McXross
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

import xyz.mcxross.ksui.account.Secp256k1Account
import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.Result

/**
 * This class represents an Secp256k1 private key.
 *
 * Creating a private key instance by either generating or _importing_ will not generate a
 * passphrase. If you want to generate a passphrase, you can use the [Account.create] method.
 *
 * @property privateKey The private key data.
 */
class Secp256k1PrivateKey(private val privateKey: ByteArray) : PrivateKey {

  override val data: ByteArray
    get() = privateKey

  override val publicKey: Secp256k1PublicKey
    get() = derivePublicKey(this, SignatureScheme.Secp256k1) as Secp256k1PublicKey

  /**
   * Creates a new [Secp256k1PrivateKey] with a randomly generated private key.
   *
   * Default signature scheme is [SignatureScheme.Secp256k1].
   *
   * @param scheme The signature scheme to use.
   * @throws IllegalArgumentException If the signature scheme is not supported.
   */
  constructor(
    scheme: SignatureScheme = SignatureScheme.Secp256k1
  ) : this(
    derivePrivateKeyFromMnemonic(
      generateMnemonic().split(" "),
      SignatureScheme.Secp256k1,
      Secp256k1Account.DERIVATION_PATH,
    )
  )

  /**
   * Creates a new [Secp256k1PrivateKey] from an encoded private key.
   *
   * The expected format is a Bech32 encoded private key.
   *
   * @param privateKey The encoded private key.
   * @throws IllegalArgumentException If the private key is invalid.
   */
  constructor(privateKey: String) : this(PrivateKey.fromEncoded(privateKey).data)

  override fun sign(data: ByteArray): Result<ByteArray, E> {
    return sign(data, this)
  }
}

class Secp256k1PublicKey(override val data: ByteArray) : PublicKey {
  override fun scheme(): SignatureScheme {
    return SignatureScheme.Secp256k1
  }

  override fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    verifySignature(this, message, signature)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Secp256k1PublicKey

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return Hex(hash(Hash.BLAKE2B256, data)).toString()
  }
}
