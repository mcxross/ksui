package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class SuiCoinMetadata(
  val decimals: UByte,
  val description: String,
  val iconUrl: String?,
  val id: String = "",
  val name: String,
  val symbol: String,
)

@Serializable data class Supply(val value: Long)

@Serializable
data class CoinData(
  val coinType: String,
  val coinObjectId: String,
  val version: Long,
  val digest: String,
  val balance: Long,
  val lockedUntilEpoch: Long? = null,
  val previousTransaction: String?,
) {
  fun objectReference() =
    ObjectReference(Reference(AccountAddress(coinObjectId)), version, ObjectDigest(Digest(digest)))
}

@Serializable
data class CoinPage(
  val data: List<CoinData> = emptyList(),
  val nextCursor: String? = null,
  val hasNextPage: Boolean,
) {
  fun at(index: Int = 0): ObjectReference {
    val coin = data.getOrNull(index)
    if (coin != null) {
      return coin.objectReference()
    } else {
      throw IndexOutOfBoundsException("Index $index is out of bounds.")
    }
  }

  infix fun pick(index: Int) = at(index)
}
