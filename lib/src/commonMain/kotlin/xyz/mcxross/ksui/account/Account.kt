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
import xyz.mcxross.ksui.core.crypto.PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.importFromMnemonic
import xyz.mcxross.ksui.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.model.AccountAddress

/**
 * This file defines the `Account` abstract class and its companion object, which provides methods
 * for creating and importing accounts using different signature schemes.
 *
 * The `[Account]` class has the following abstract properties:
 * - `[publicKey]`: The public key of the account.
 * - `[address]`: The account address.
 * - `[scheme]`: The signature scheme used by the account.
 *
 * The companion object provides the following methods:
 * - `create(scheme: SignatureScheme = SignatureScheme.ED25519)`: Creates a new account using the
 *   specified signature scheme. Defaults to ED25519.
 * - `import(privateKey: ByteArray, scheme: SignatureScheme = SignatureScheme.ED25519)`: Imports an
 *   account using the provided private key and signature scheme. Defaults to ED25519.
 * - `import(phrase: String, scheme: SignatureScheme = SignatureScheme.ED25519)`: Imports an account
 *   using the provided mnemonic phrase and signature scheme. Defaults to ED25519.
 * - `import(phrases: List<String>, scheme: SignatureScheme = SignatureScheme.ED25519)`: Imports an
 *   account using the provided list of mnemonic phrases and signature scheme. Defaults to ED25519.
 *
 * The `[create]` and `[import]` methods throw a `[SignatureSchemeNotSupportedException]` if the
 * specified signature scheme is not supported.
 */
abstract class Account {

  abstract val publicKey: PublicKey

  abstract val address: AccountAddress

  abstract val scheme: SignatureScheme

  companion object {

    /**
     * Creates a new account using the specified signature scheme.
     *
     * @param scheme The signature scheme to use. Defaults to ED25519.
     * @return The new account.
     * @throws SignatureSchemeNotSupportedException If the specified signature scheme is not
     *   supported.
     */
    fun create(scheme: SignatureScheme = SignatureScheme.ED25519): Account {
      return when (scheme) {
        SignatureScheme.ED25519 -> Ed25519Account.generate()
        else -> throw SignatureSchemeNotSupportedException()
      }
    }

    /**
     * Imports an account using the provided private key and signature scheme.
     *
     * @param privateKey The private key of the account.
     * @param scheme The signature scheme to use. Defaults to ED25519.
     * @return The imported account.
     * @throws SignatureSchemeNotSupportedException If the specified signature scheme is not
     *   supported.
     */
    fun import(privateKey: ByteArray, scheme: SignatureScheme = SignatureScheme.ED25519): Account {
      return when (scheme) {
        SignatureScheme.ED25519 -> Ed25519Account(Ed25519PrivateKey(privateKey))
        else -> throw SignatureSchemeNotSupportedException()
      }
    }

    /**
     * Imports an account using the provided mnemonic phrase and signature scheme.
     *
     * @param phrase The mnemonic phrase of the account.
     * @param scheme The signature scheme to use. Defaults to ED25519.
     * @return The imported account.
     * @throws SignatureSchemeNotSupportedException If the specified signature scheme is not
     *   supported.
     */
    fun import(phrase: String, scheme: SignatureScheme = SignatureScheme.ED25519): Account {
      return when (scheme) {
        SignatureScheme.ED25519 -> {
          val keyPair = importFromMnemonic(phrase)
          Ed25519Account(Ed25519PrivateKey(keyPair.privateKey), phrase)
        }
        else -> throw SignatureSchemeNotSupportedException()
      }
    }

    /**
     * Imports an account using the provided list of mnemonic phrases and signature scheme.
     *
     * @param phrases The list of mnemonic phrases of the account.
     * @param scheme The signature scheme to use. Defaults to ED25519.
     * @return The imported account.
     * @throws SignatureSchemeNotSupportedException If the specified signature scheme is not
     *   supported.
     */
    fun import(phrases: List<String>, scheme: SignatureScheme = SignatureScheme.ED25519): Account {
      return when (scheme) {
        SignatureScheme.ED25519 -> {
          import(phrases.joinToString(" "))
        }
        else -> throw SignatureSchemeNotSupportedException()
      }
    }
  }
}
