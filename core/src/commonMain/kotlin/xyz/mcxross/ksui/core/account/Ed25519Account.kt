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
import xyz.mcxross.ksui.core.crypto.Ed25519PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.crypto.generateMnemonic
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result

class Ed25519Account(
  private val privateKey: Ed25519PrivateKey,
  val mnemonic: String,
) : Account() {

  constructor(privateKey: Ed25519PrivateKey) : this(privateKey, "")

  override val publicKey: Ed25519PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress by lazy { AccountAddress.fromPublicKey(publicKey) }

  override val scheme: SignatureScheme
    get() = SignatureScheme.ED25519

  override suspend fun sign(message: ByteArray): Result<ByteArray, E> = privateKey.sign(message)

  override suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    publicKey.verify(message, signature)

  override fun close() {
    privateKey.close()
  }

  override fun toString(): String = "Ed25519Account{address=$address}"

  companion object {
    fun generate(): Ed25519Account {
      val mnemonicPhrase = generateMnemonic().split(" ")
      val privateKeyBytes = derivePrivateKeyFromMnemonic(mnemonicPhrase, SignatureScheme.ED25519)
      val privateKey = Ed25519PrivateKey(privateKeyBytes)
      privateKeyBytes.fill(0)
      return Ed25519Account(privateKey, mnemonicPhrase.joinToString(" "))
    }
  }
}
