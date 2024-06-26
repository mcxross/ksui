package xyz.mcxross.ksui.sample

import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64

abstract class SuiKeyPair<T> {
  /**
   * Gets key pair.
   *
   * @return the key pair
   */
  /** The Key pair. */
  var keyPair: T? = null
    protected set

  override fun toString(): String {
    return "SuiKeyPair{keyPair=$keyPair}"
  }

  /**
   * Address string.
   *
   * @return the string
   */
  abstract fun address(): String

  /**
   * Public key string.
   *
   * @return the string
   */
  fun publicKey(): String {
    return Base64.toBase64String(this.publicKeyBytes())
  }

  /**
   * Public key byte [ ].
   *
   * @return the byte [ ]
   */
  abstract fun publicKeyBytes(): ByteArray?

  /**
   * Signature scheme signature scheme.
   *
   * @return the signature scheme
   */
  abstract fun signatureScheme(): SignatureScheme?

  @Throws(SigningException::class)
  fun sign(msg: String?): String {
    val msgBytes = Base64.decode(msg)
    return Base64.toBase64String(this.sign(msgBytes))
  }

  /**
   * Sign string.
   *
   * @param msg the msg
   * @return the string
   * @throws SigningException the signing exception
   */
  @Throws(SigningException::class) abstract fun sign(msg: ByteArray?): ByteArray

  /**
   * encode base64 sui key.
   *
   * @return the sui key
   */
  abstract fun encodePrivateKey(): String?

  companion object {
    init {
      if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
        Security.addProvider(BouncyCastleProvider())
      }
    }

    /**
     * Decode base64 sui key pair.
     *
     * @param encoded the encoded
     * @return the sui key pair
     * @throws SignatureSchemeNotSupportedException the signature scheme not supported exception
     */
    @Throws(SignatureSchemeNotSupportedException::class)
    fun decodeBase64(encoded: String?): SuiKeyPair<*> {
      val keyPairBytes = Base64.decode(encoded)

      val scheme =
        SignatureScheme.valueOf(keyPairBytes[0]) ?: throw SignatureSchemeNotSupportedException()
      return when (scheme) {
        SignatureScheme.ED25519 -> ED25519KeyPair.decodeBase64(keyPairBytes)
        else -> throw SignatureSchemeNotSupportedException()
      }
    }
  }
}
