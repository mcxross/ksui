package xyz.mcxross.ksui.core.crypto

class PasskeyPublicKey(override val data: ByteArray) : PublicKey {
  override fun scheme(): SignatureScheme = SignatureScheme.PASSKEY

  override fun verify(message: ByteArray, signature: ByteArray): Boolean {
    return verifySignature(this, message, signature)
  }
}
