package org.komputing.kbech32

data class Bech32Data(val humanReadablePart: String, val data: ByteArray) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Bech32Data

    if (humanReadablePart != other.humanReadablePart) return false
    if (!data.contentEquals(other.data)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = humanReadablePart.hashCode()
    result = 31 * result + data.contentHashCode()
    return result
  }
}
