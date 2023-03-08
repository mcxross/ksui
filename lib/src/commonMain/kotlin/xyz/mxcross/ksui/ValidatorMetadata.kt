package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidatorMetadata(
  @SerialName("sui_address") val suiAddress: String,
  @SerialName("pubkey_bytes") val pubkeyBytes: ByteArray,
  @SerialName("network_pubkey_bytes") val networkPubkeyBytes: ByteArray,
  @SerialName("worker_pubkey_bytes") val workerPubkeyBytes: ByteArray,
  @SerialName("proof_of_possession_bytes") val proofOfPossessionBytes: ByteArray,
  val name: String,
  val description: String,
  @SerialName("image_url") val imageUrl: String,
  @SerialName("project_url") val projectUrl: String,
  @SerialName("net_address") val netAddress: List<Int>,
  @SerialName("consensus_address") val consensusAddress: List<Int>,
  @SerialName("worker_address") val workerAddress: List<Int>,
  @SerialName("next_epoch_stake") val nextEpochStake: Int,
  @SerialName("next_epoch_delegation") val nextEpochDelegation: Long,
  @SerialName("next_epoch_gas_price") val nextEpochGasPrice: Int,
  @SerialName("next_epoch_commission_rate") val nextEpochCommissionRate: Int
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as SuiValidatorSummary

    if (suiAddress != other.suiAddress) return false
    if (!pubkeyBytes.contentEquals(other.pubkeyBytes)) return false
    if (!networkPubkeyBytes.contentEquals(other.networkPubkeyBytes)) return false
    if (!workerPubkeyBytes.contentEquals(other.workerPubkeyBytes)) return false
    if (!proofOfPossessionBytes.contentEquals(other.proofOfPossessionBytes)) return false
    if (name != other.name) return false
    if (description != other.description) return false
    if (imageUrl != other.imageUrl) return false
    if (projectUrl != other.projectUrl) return false
    if (netAddress != other.netAddress) return false
    if (consensusAddress != other.consensusAddress) return false
    if (workerAddress != other.workerAddress) return false
    if (nextEpochStake != other.nextEpochStake) return false
    if (nextEpochDelegation != other.nextEpochDelegation) return false
    if (nextEpochGasPrice != other.nextEpochGasPrice) return false
    if (nextEpochCommissionRate != other.nextEpochCommissionRate) return false

    return true
  }

  override fun hashCode(): Int {
    var result = suiAddress.hashCode()
    result = 31 * result + pubkeyBytes.contentHashCode()
    result = 31 * result + networkPubkeyBytes.contentHashCode()
    result = 31 * result + workerPubkeyBytes.contentHashCode()
    result = 31 * result + proofOfPossessionBytes.contentHashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + imageUrl.hashCode()
    result = 31 * result + projectUrl.hashCode()
    result = 31 * result + netAddress.hashCode()
    result = 31 * result + consensusAddress.hashCode()
    result = 31 * result + workerAddress.hashCode()
    result = 31 * result + nextEpochStake
    result = 31 * result + nextEpochDelegation.hashCode()
    result = 31 * result + nextEpochGasPrice
    result = 31 * result + nextEpochCommissionRate
    return result
  }
}
