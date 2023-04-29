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
    val lockedUntilEpoch: Long?,
    val previousTransaction: String?,
)

@Serializable
data class CoinPage(
    val data: List<CoinData>,
    val nextCursor: String,
    val hasNextPage: Boolean,
)
