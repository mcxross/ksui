package xyz.mcxross.ksui.core.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.core.serializer.AccountAddressSerializer

@Serializable(with = AccountAddressSerializer::class)
data class AccountAddress(val data: ByteArray) {

  @Throws(IllegalArgumentException::class) constructor(s: String) : this(fromString(s).data)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as AccountAddress
    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int = data.contentHashCode()

  override fun toString(): String = Hex(data).toString()

  companion object {

    const val LENGTH: Int = 32

    val EMPTY: AccountAddress
      get() = AccountAddress(ByteArray(LENGTH))

    @Throws(IllegalArgumentException::class)
    fun from(data: ByteArray): AccountAddress {
      require(data.size == LENGTH) {
        "Address must be $LENGTH bytes long, but was ${data.size}"
      }
      return AccountAddress(data.copyOf())
    }

    @Throws(IllegalArgumentException::class)
    fun fromString(s: String): AccountAddress {
      return try {
        fromHexLiteral(s)
      } catch (_: Exception) {
        fromHex(s)
      }
    }

    @Throws(IllegalArgumentException::class)
    fun fromPublicKey(publicKey: PublicKey): AccountAddress {
      val pubKeyBytes = publicKey.data
      when (publicKey.scheme()) {
        SignatureScheme.ED25519 ->
          require(pubKeyBytes.size == 32) {
            "Ed25519 public key must be 32 bytes long, but was ${pubKeyBytes.size}"
          }
        SignatureScheme.Secp256k1 ->
          require(pubKeyBytes.size == 33) {
            "Secp256k1 public key must be 33 bytes long, but was ${pubKeyBytes.size}"
          }
        SignatureScheme.Secp256r1 ->
          require(pubKeyBytes.size == 33) {
            "Secp256r1 public key must be 33 bytes long, but was ${pubKeyBytes.size}"
          }
        SignatureScheme.PASSKEY ->
          require(pubKeyBytes.size == 33) {
            "Passkey public key must be 33 bytes long, but was ${pubKeyBytes.size}"
          }
        else ->
          throw IllegalArgumentException("Unsupported public key scheme: ${publicKey.scheme()}")
      }
      val payload = ByteArray(1 + pubKeyBytes.size)
      payload[0] = publicKey.scheme().scheme
      pubKeyBytes.copyInto(payload, destinationOffset = 1)
      return AccountAddress(hash(Hash.BLAKE2B256, payload))
    }

    private fun fromHexLiteral(literal: String): AccountAddress {
      require(literal.startsWith("0x") || literal.startsWith("0X")) { "Address must start with 0x" }
      val body = literal.substring(2)
      require(body.isNotEmpty() && body.length <= LENGTH * 2) {
        "Invalid hex literal length: ${body.length}"
      }
      return fromHex(body.padStart(LENGTH * 2, '0'))
    }

    private fun fromHex(hex: String): AccountAddress {
      require(hex.length == LENGTH * 2) {
        "Address must be $LENGTH bytes long, but hex length was ${hex.length}"
      }
      val bytes = ByteArray(LENGTH) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
      return AccountAddress(bytes)
    }
  }
}
