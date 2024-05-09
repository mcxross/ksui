package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.SuiAddressSerializer
import xyz.mcxross.ksui.util.LENGTH

@Serializable
data class SuiAddress(@Serializable(with = SuiAddressSerializer::class) val data: ByteArray) {

  constructor(data: String) : this(fromString(data).data)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as SuiAddress

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return data.joinToString("") { it.toInt().and(0xff).toString(16).padStart(2, '0') }
  }

  companion object {
    val EMPTY = SuiAddress(ByteArray(LENGTH))

    fun fromString(data: String): SuiAddress {
      val bytes =
        data.let { bytesString ->
          val c = if (bytesString.startsWith("0x")) bytesString.substring(2) else bytesString
          val byteArray =
            ByteArray(c.length / 2) { c.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
          require(byteArray.size == LENGTH) {
            "Address must be $LENGTH bytes long, but was ${byteArray.size}"
          }
          byteArray
        }
      return SuiAddress(bytes)
    }
  }
}
