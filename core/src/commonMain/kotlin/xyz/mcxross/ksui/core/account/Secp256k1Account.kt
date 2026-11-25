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
package xyz.mcxross.ksui.core.account

import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.crypto.generateMnemonic
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result

class Secp256k1Account(
  private val privateKey: Secp256k1PrivateKey,
  val mnemonic: String,
) : Account() {

  constructor(privateKey: Secp256k1PrivateKey) : this(privateKey, "")

  override val publicKey: Secp256k1PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress by lazy { AccountAddress.fromPublicKey(publicKey) }

  override val scheme: SignatureScheme
    get() = SignatureScheme.Secp256k1

  override suspend fun sign(message: ByteArray): Result<ByteArray, E> = privateKey.sign(message)

  override suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    publicKey.verify(message, signature)

  override fun close() {
    privateKey.close()
  }

  override fun toString(): String = "Secp256k1Account{address=$address}"

  companion object {
    const val DERIVATION_PATH = "m/54'/784'/0'/0/0"

    fun generate(): Secp256k1Account {
      val seedPhrase = generateMnemonic().split(" ")
      val finalPrivateKeyBytes =
        derivePrivateKeyFromMnemonic(seedPhrase, SignatureScheme.Secp256k1, DERIVATION_PATH)
      val privateKey = Secp256k1PrivateKey(finalPrivateKeyBytes)
      finalPrivateKeyBytes.fill(0)
      return Secp256k1Account(privateKey, seedPhrase.joinToString(" "))
    }
  }
}
