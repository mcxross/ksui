package xyz.mcxross.ksui.core.crypto

import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.Result

class Secp256r1PrivateKey(privateKey: ByteArray) : PrivateKey {
  private val keyBytes: ByteArray =
    privateKey.copyOf().also {
      require(it.size == 32) { "Secp256r1 private key must be 32 bytes, but was ${it.size}" }
    }
  private var closed: Boolean = false

  override val data: ByteArray
    get() {
      check(!closed) { "Private key is closed" }
      return keyBytes.copyOf()
    }

  override val publicKey: Secp256r1PublicKey by lazy {
    derivePublicKey(this, SignatureScheme.Secp256r1) as Secp256r1PublicKey
  }

  constructor(
    scheme: SignatureScheme = SignatureScheme.Secp256r1
  ) : this(generateSecp256r1KeyBytes(scheme))

  constructor(privateKey: String) : this(decodeSecp256r1KeyBytes(privateKey))

  override fun sign(data: ByteArray): Result<ByteArray, E> {
    if (closed) return Result.Err(IllegalStateException("Private key is closed"))
    return sign(data, this)
  }

  override fun close() {
    keyBytes.fill(0)
    closed = true
  }

  companion object {
    private fun generateSecp256r1KeyBytes(scheme: SignatureScheme): ByteArray {
      require(scheme == SignatureScheme.Secp256r1) {
        "Unsupported scheme for Secp256r1PrivateKey: $scheme"
      }
      return randomEcdsaSecretKey(SECP256R1_ORDER)
    }

    private fun decodeSecp256r1KeyBytes(encoded: String): ByteArray {
      val parsed = PrivateKey.fromEncoded(encoded)
      try {
        require(parsed is Secp256r1PrivateKey) {
          "Expected Secp256r1 private key, but got ${parsed.publicKey.scheme()}"
        }
        return parsed.data
      } finally {
        parsed.close()
      }
    }
  }
}

class Secp256r1PublicKey(data: ByteArray) : PublicKey {
  private val keyBytes: ByteArray = data.copyOf()

  override val data: ByteArray
    get() = keyBytes.copyOf()

  override fun scheme(): SignatureScheme = SignatureScheme.Secp256r1

  override fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    verifySignature(this, message, signature)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as Secp256r1PublicKey
    return keyBytes.contentEquals(other.keyBytes)
  }

  override fun hashCode(): Int = keyBytes.contentHashCode()

  // Not an address: Sui addresses are blake2b(flag || key). Use AccountAddress.fromPublicKey.
  override fun toString(): String = "Secp256r1PublicKey(${Hex(keyBytes)})"
}
