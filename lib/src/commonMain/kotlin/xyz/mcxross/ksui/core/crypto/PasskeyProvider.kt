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

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import xyz.mcxross.ksui.account.PasskeyAccount
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.Result

expect class PasskeyProvider {
  /**
   * Creates a new Passkey credential.
   *
   * Note: For a consistent user experience, it is highly recommended to provide a stable, unique
   * `userId` for each user. Using the default random UUID will result in a new passkey being
   * created on every call.
   *
   * @param name A user-friendly name for the account (often the same as displayName).
   * @param displayName The name displayed to the user in the system's passkey creation prompt.
   * @param userId A stable, unique identifier for the user, used to link the passkey to their
   *   account.
   * @param challenge A random, unique, single-use string sent from the server to prevent replay
   *   attacks.
   * @return A PasskeyAccount containing the new public key.
   */
  @OptIn(ExperimentalUuidApi::class)
  internal suspend fun create(
    name: String,
    displayName: String = name,
    userId: String = Uuid.random().toHexDashString(),
    challenge: String = Uuid.random().toHexDashString(),
  ): Result<PasskeyAccount, E>

  /**
   * Signs a challenge with an existing Passkey.
   *
   * @param challenge The 32-byte hash to be signed.
   * @return The full serialized Passkey signature payload.
   */
  internal suspend fun sign(pk: ByteArray, challenge: ByteArray): Result<ByteArray, E>
}
