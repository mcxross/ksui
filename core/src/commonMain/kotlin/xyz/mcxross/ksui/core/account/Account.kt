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
package xyz.mcxross.ksui.core.account

import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey
import xyz.mcxross.ksui.core.crypto.PrivateKey
import xyz.mcxross.ksui.core.crypto.PublicKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256r1PrivateKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.crypto.importFromMnemonic
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result

abstract class Account : AutoCloseable {

  abstract val publicKey: PublicKey

  abstract val address: AccountAddress

  abstract val scheme: SignatureScheme

  override fun close() {}

  @Throws(Exception::class) abstract suspend fun sign(message: ByteArray): Result<ByteArray, E>

  @Throws(Exception::class)
  abstract suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E>

  companion object {

    @Throws(Exception::class)
    fun create(scheme: SignatureScheme = SignatureScheme.ED25519): Account =
      when (scheme) {
        SignatureScheme.ED25519 -> Ed25519Account.generate()
        SignatureScheme.Secp256k1 -> Secp256k1Account.generate()
        SignatureScheme.Secp256r1 -> Secp256r1Account.generate()
        else -> throw SignatureSchemeNotSupportedException()
      }

    @Throws(Exception::class)
    fun import(privateKey: ByteArray, scheme: SignatureScheme = SignatureScheme.ED25519): Account =
      when (scheme) {
        SignatureScheme.ED25519 -> Ed25519Account(Ed25519PrivateKey(privateKey))
        SignatureScheme.Secp256k1 -> Secp256k1Account(Secp256k1PrivateKey(privateKey))
        SignatureScheme.Secp256r1 -> Secp256r1Account(Secp256r1PrivateKey(privateKey))
        else -> throw SignatureSchemeNotSupportedException()
      }

    @Throws(Exception::class)
    fun import(str: String): Account {
      val trimmed = str.trim()
      if (trimmed.contains(" ")) {
        return importMnemonic(trimmed, SignatureScheme.ED25519)
      }
      return import(PrivateKey.fromEncoded(trimmed))
    }

    @Throws(Exception::class)
    fun import(str: String, scheme: SignatureScheme): Account {
      val trimmed = str.trim()
      if (trimmed.contains(" ")) {
        return importMnemonic(trimmed, scheme)
      }
      val key = PrivateKey.fromEncoded(trimmed)
      if (key.publicKey.scheme() != scheme) {
        key.close()
        throw IllegalArgumentException(
          "Encoded private key scheme ${key.publicKey.scheme()} does not match requested $scheme",
        )
      }
      return import(key)
    }

    @Throws(Exception::class)
    fun import(phrases: List<String>, scheme: SignatureScheme = SignatureScheme.ED25519): Account =
      importMnemonic(phrases.joinToString(" ").trim(), scheme)

    @Throws(Exception::class)
    fun import(privateKey: PrivateKey): Account =
      when (privateKey) {
        is Ed25519PrivateKey -> Ed25519Account(privateKey)
        is Secp256k1PrivateKey -> Secp256k1Account(privateKey)
        is Secp256r1PrivateKey -> Secp256r1Account(privateKey)
        else -> throw SignatureSchemeNotSupportedException()
      }

    private fun importMnemonic(phrase: String, scheme: SignatureScheme): Account {
      val words = phrase.split(Regex("\\s+")).filter { it.isNotEmpty() }
      val normalizedPhrase = words.joinToString(" ")
      return when (scheme) {
        SignatureScheme.ED25519 -> {
          val keyPair = importFromMnemonic(normalizedPhrase)
          try {
            Ed25519Account(Ed25519PrivateKey(keyPair.privateKey), normalizedPhrase)
          } finally {
            keyPair.privateKey.fill(0)
          }
        }
        SignatureScheme.Secp256k1 -> {
          val keyBytes =
            derivePrivateKeyFromMnemonic(
              words,
              SignatureScheme.Secp256k1,
              Secp256k1Account.DERIVATION_PATH,
            )
          try {
            Secp256k1Account(Secp256k1PrivateKey(keyBytes), normalizedPhrase)
          } finally {
            keyBytes.fill(0)
          }
        }
        SignatureScheme.Secp256r1 -> {
          val keyBytes =
            derivePrivateKeyFromMnemonic(
              words,
              SignatureScheme.Secp256r1,
              Secp256r1Account.DERIVATION_PATH,
            )
          try {
            Secp256r1Account(Secp256r1PrivateKey(keyBytes), normalizedPhrase)
          } finally {
            keyBytes.fill(0)
          }
        }
        else -> throw SignatureSchemeNotSupportedException()
      }
    }
  }
}
