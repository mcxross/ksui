package xyz.mcxross.ksui.sample

import com.google.common.primitives.Bytes
import org.apache.commons.lang3.StringUtils
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.CryptoException
import org.bouncycastle.crypto.Signer
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex

class ED25519KeyPair(
  privateKeyParameters: Ed25519PrivateKeyParameters?,
  publicKeyParameters: Ed25519PublicKeyParameters?,
) : SuiKeyPair<AsymmetricCipherKeyPair?>() {
  /**
   * Instantiates a new Ed 25519 key pair.
   *
   * @param privateKeyParameters the private key parameters
   * @param publicKeyParameters the public key parameters
   */
  init {
    this.keyPair = AsymmetricCipherKeyPair(publicKeyParameters, privateKeyParameters)
  }

  override fun address(): String {
    val blake2b256 = Blake2b.Blake2b256()
    val hash =
      blake2b256.digest(
        Arrays.prepend(
          (keyPair!!.public as Ed25519PublicKeyParameters).encoded,
          SignatureScheme.ED25519.scheme,
        )
      )
    return "0x" + StringUtils.substring(Hex.toHexString(hash), 0, 64)
  }

  override fun publicKeyBytes(): ByteArray? {
    return (keyPair!!.public as Ed25519PublicKeyParameters).encoded
  }

  override fun signatureScheme(): SignatureScheme? {
    return SignatureScheme.ED25519
  }

  @Throws(SigningException::class)
  override fun sign(msg: ByteArray?): ByteArray {
    val signer: Signer = Ed25519Signer()
    signer.init(true, keyPair!!.private)
    signer.update(msg, 0, msg!!.size)
    try {
      return signer.generateSignature()
    } catch (e: CryptoException) {
      throw SigningException(e)
    }
  }

  /**
   * Encode base 64 sui key.
   *
   * @return the sui key
   */
  override fun encodePrivateKey(): String? {
    val pair = keyPair!!.private as Ed25519PrivateKeyParameters
    val data = Bytes.concat(byteArrayOf(SignatureScheme.ED25519.scheme), pair.encoded)
    return Base64.toBase64String(data)
  }

  companion object {
    /**
     * Decode base 64 sui key pair.
     *
     * @param encoded the encoded
     * @return the sui key pair
     */
    fun decodeBase64(encoded: ByteArray?): ED25519KeyPair {
      val privateKeyParameters = Ed25519PrivateKeyParameters(encoded, 1)
      val publicKeyParameters = privateKeyParameters.generatePublicKey()
      return ED25519KeyPair(privateKeyParameters, publicKeyParameters)
    }
  }
}
