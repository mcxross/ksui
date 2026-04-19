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

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import xyz.mcxross.ksui.core.crypto.PasskeyProvider
import xyz.mcxross.ksui.core.crypto.PasskeyPublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.verifySignature
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

class PasskeyAccount(
  override val publicKey: PasskeyPublicKey,
  private val provider: PasskeyProvider,
) : Account() {
  override val address: AccountAddress
    get() = AccountAddress.fromPublicKey(publicKey)

  override val scheme: SignatureScheme
    get() = SignatureScheme.PASSKEY

  override suspend fun sign(message: ByteArray): Result<ByteArray, E> {
    return provider.sign(publicKey.data, message)
  }

  override suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> {
    return verifySignature(publicKey, message, signature)
  }

  companion object {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun create(
      provider: PasskeyProvider,
      name: String,
      displayName: String = name,
      userId: String = Uuid.random().toHexDashString(),
      challenge: String = Uuid.random().toHexDashString(),
    ): Result<PasskeyAccount, E> {
      return provider.create(name, displayName, userId, challenge)
    }
  }
}
