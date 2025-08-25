package xyz.mcxross.ksui.account

import xyz.mcxross.ksui.core.crypto.PasskeyProvider
import xyz.mcxross.ksui.core.crypto.PasskeyPublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.model.AccountAddress

class PasskeyAccount(
  override val publicKey: PasskeyPublicKey,
  private val provider: PasskeyProvider,
) : Account() {
  override val address: AccountAddress
    get() = AccountAddress.fromPublicKey(publicKey)

  override val scheme: SignatureScheme
    get() = SignatureScheme.PASSKEY

  override suspend fun sign(message: ByteArray): ByteArray {
    return provider.sign(message)
  }

  override suspend fun verify(message: ByteArray, signature: ByteArray): Boolean {
    TODO("Not yet implemented")
  }
}
