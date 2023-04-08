package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidatorMetadata(
  @SerialName("sui_address") val suiAddress: String,
  @SerialName("pubkey_bytes") val pubkeyBytes: List<Int>,
  @SerialName("network_pubkey_bytes") val networkPubkeyBytes: List<Int>,
  @SerialName("worker_pubkey_bytes") val workerPubkeyBytes: List<Int>,
  @SerialName("proof_of_possession_bytes") val proofOfPossessionBytes: List<Int>,
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
)

@Serializable data class Validators(@SerialName("result") val list: List<ValidatorMetadata>)

@Serializable data class ValidatorReportRecord(val hash: String, val addresses: List<String>)

@Serializable data class Validator(val publicKey: String, val weight: Int)
