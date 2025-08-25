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
package xyz.mcxross.ksui.account

import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey
import xyz.mcxross.ksui.core.crypto.Ed25519PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.crypto.generateMnemonic
import xyz.mcxross.ksui.model.AccountAddress

/**
 * This file defines the `Ed25519Account` class, which extends the `Account` abstract class and
 * provides specific implementations for the Ed25519 signature scheme.
 *
 * The `Ed25519Account` class has the following properties:
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
 * - `generate()`: Generates a new `Ed25519Account` using a randomly generated mnemonic phrase and
 *   seed.
 */
class Ed25519Account(private val privateKey: Ed25519PrivateKey) : Account() {

  var mnemonic: String = ""

  override val publicKey: Ed25519PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress
    get() = AccountAddress.fromPublicKey(publicKey)

  override val scheme: SignatureScheme
    get() = SignatureScheme.ED25519

  override suspend fun sign(message: ByteArray): ByteArray {
    return privateKey.sign(message)
  }

  override suspend fun verify(message: ByteArray, signature: ByteArray): Boolean =
    publicKey.verify(message, signature)

  constructor(privateKey: Ed25519PrivateKey, mnemonic: String) : this(privateKey) {
    this.mnemonic = mnemonic
  }

  override fun toString(): String {
    return "Ed25519Account{mnemonic=$mnemonic, address=$address}"
  }

  companion object {

    /**
     * Generates a new `Ed25519Account` using a randomly generated mnemonic phrase and seed.
     *
     * @return The new `Ed25519Account`.
     */
    fun generate(): Ed25519Account {
      val mnemonicPhrase = generateMnemonic().split(" ")
      val privateKeyBytes = derivePrivateKeyFromMnemonic(mnemonicPhrase, SignatureScheme.ED25519)
      val privateKey = Ed25519PrivateKey(privateKeyBytes)
      return Ed25519Account(privateKey, mnemonicPhrase.joinToString(" "))
    }
  }
}
