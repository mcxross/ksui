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
package xyz.mcxross.ksui.account

import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.crypto.generateMnemonic
import xyz.mcxross.ksui.model.AccountAddress

/**
 * This file defines the `Secp256k1Account` class, which extends the `Account` abstract class and
 * provides specific implementations for the Ed25519 signature scheme.
 *
 * The `Secp256k1Account` class has the following properties:
 * - `privateKey`: The private key of the account.
 * - `mnemonic`: A string representing the mnemonic phrase associated with the account.
 * - `publicKey`: The public key of the account, derived from the private key.
 * - `address`: The account address, derived from the public key and the signature scheme.
 * - `scheme`: The signature scheme used by the account, which is Ed25519.
 *
 * The class provides the following methods:
 * - `toString()`: Returns a string representation of the account, including the mnemonic and
 *   address.
 *
 * The companion object provides the following methods:
 * - `generate()`: Generates a new `Secp256k1Account` using a randomly generated mnemonic phrase and
 *   seed.
 */
class Secp256k1Account(private val privateKey: Secp256k1PrivateKey) : Account() {

  var mnemonic: String = ""

  override val publicKey: Secp256k1PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress
    get() = AccountAddress.fromPublicKey(publicKey)

  override val scheme: SignatureScheme
    get() = SignatureScheme.Secp256k1

  override suspend fun sign(message: ByteArray): ByteArray {
    return privateKey.sign(message)
  }

  override suspend fun verify(message: ByteArray, signature: ByteArray): Boolean =
    publicKey.verify(message, signature)

  constructor(privateKey: Secp256k1PrivateKey, mnemonic: String) : this(privateKey) {
    this.mnemonic = mnemonic
  }

  override fun toString(): String {
    return "Secp256k1Account{mnemonic=$mnemonic, address=$address}"
  }

  companion object {

    const val DERIVATION_PATH = "m/54'/784'/0'/0/0"

    /**
     * Generates a new `Secp256k1Account` using a randomly generated mnemonic phrase and seed.
     *
     * @return The new `Secp256k1Account`.
     */
    fun generate(): Secp256k1Account {
      val seedPhrase = generateMnemonic().split(" ")
      val finalPrivateKeyBytes =
        derivePrivateKeyFromMnemonic(seedPhrase, SignatureScheme.Secp256k1, DERIVATION_PATH)
      val privateKey = Secp256k1PrivateKey(finalPrivateKeyBytes)
      return Secp256k1Account(privateKey, seedPhrase.joinToString(" "))
    }
  }
}
