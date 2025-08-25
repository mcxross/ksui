package xyz.mcxross.ksui.core.crypto

import xyz.mcxross.ksui.account.PasskeyAccount


expect class PasskeyProvider {
  /**
   * Creates a new Passkey credential.
   * @param name A user-friendly name for the account.
   * @return A PasskeyAccount containing the new public key.
   */
  suspend fun create(name: String): PasskeyAccount

  /**
   * Signs a challenge with an existing Passkey.
   * @param challenge The 32-byte hash to be signed.
   * @return The full serialized Passkey signature payload.
   */
  suspend fun sign(challenge: ByteArray): ByteArray
}
