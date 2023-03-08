package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class CheckpointId(
  val digest: CheckpointDigest,
)

@Serializable
data class CheckpointDigest(
  val digest: Digest,
)

@Serializable data class CheckpointContentsDigest(val digest: CheckpointDigest)

@Serializable
data class CheckpointSequenceNumber(
  val sequenceNumber: Long,
)

data class CheckpointContents(
  val transactions: List<Transaction>,
  val userSignatures: List<String>
)

@Serializable
class VersionSpecificData(
  val version: Int,
  val data: ByteArray,
  val versionSpecificData: ByteArray = byteArrayOf()
) {
  companion object {
    private const val CURRENT_VERSION = 1
    private const val VERSION_OFFSET = 0
    private const val DATA_OFFSET = 4
    private const val VERSION_SPECIFIC_DATA_OFFSET = 8
    private const val HEADER_SIZE = 8
  }
}

@Serializable
data class CheckpointSummary(
  val contentsDigest: CheckpointContentsDigest,
  val endOfEpochData: EndOfEpochData,
  val epoch: Long,
  val epochRollingGasCostSummary: GasCostSummary,
  val networkTotalTransactions: Long,
  val previousDigest: CheckpointDigest,
  val sequenceNumber: Long,
  val timestampMs: Long,
  val versionSpecificData: VersionSpecificData
)

data class Checkpoint(
  val digest: CheckpointDigest,
  val endOfEpochData: EndOfEpochData,
  val epoch: Long,
  val epochRollingGasCostSummary: GasCostSummary,
  val networkTotalTransactions: Long,
  val previousDigest: CheckpointDigest,
  val sequenceNumber: Long,
  val timestampMs: Long,
  val transactions: List<Transaction>
)
