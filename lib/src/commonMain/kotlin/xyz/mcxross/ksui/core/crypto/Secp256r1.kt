package xyz.mcxross.ksui.core.crypto

import xyz.mcxross.ksui.account.Secp256r1Account
import xyz.mcxross.ksui.core.Hex

class Secp256r1PrivateKey(private val privateKey: ByteArray) : PrivateKey {
  override val data: ByteArray
    get() = privateKey

  override val publicKey: Secp256r1PublicKey
    get() = derivePublicKey(this, SignatureScheme.Secp256r1) as Secp256r1PublicKey

  constructor(
    scheme: SignatureScheme = SignatureScheme.Secp256r1
  ) : this(
    derivePrivateKeyFromMnemonic(
      generateMnemonic().split(" "),
      SignatureScheme.Secp256r1,
      Secp256r1Account.DERIVATION_PATH,
    )
  )

  constructor(privateKey: String) : this(PrivateKey.fromEncoded(privateKey).data)

  override fun sign(data: ByteArray): ByteArray {
    return sign(data, this)
  }
}

class Secp256r1PublicKey(override val data: ByteArray) : PublicKey {
  override fun scheme(): SignatureScheme {
    return SignatureScheme.Secp256r1
  }

  override fun verify(message: ByteArray, signature: ByteArray): Boolean =
    verifySignature(this, message, signature)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as Secp256r1PublicKey
    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return Hex(hash(Hash.BLAKE2B256, data)).toString()
  }
}
