package xyz.mcxross.ksui.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyAuthenticator(
  val authenticatorData: ByteArray,
  val clientDataJson: String,
  val userSignature: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as PasskeyAuthenticator

    if (!authenticatorData.contentEquals(other.authenticatorData)) return false
    if (clientDataJson != other.clientDataJson) return false
    if (!userSignature.contentEquals(other.userSignature)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = authenticatorData.contentHashCode()
    result = 31 * result + clientDataJson.hashCode()
    result = 31 * result + userSignature.contentHashCode()
    return result
  }
}
