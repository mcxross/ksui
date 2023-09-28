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

@Serializable data class ValidatorApy(val address: String, val apy: Double)

@Serializable data class ValidatorApys(val apys: List<ValidatorApy>, val epoch: String)

/**
 * A summary of a validator's information.
 *
 * @property suiAddress The SUI address of the validator.
 * @property protocolPubkeyBytes The protocol public key of the validator.
 * @property networkPubkeyBytes The network public key of the validator.
 * @property workerPubkeyBytes The worker public key of the validator.
 * @property proofOfPossessionBytes The proof of possession of the validator.
 * @property name The name of the validator.
 * @property description The description of the validator.
 * @property imageUrl The image URL of the validator.
 * @property projectUrl The project URL of the validator.
 * @property netAddress The net address of the validator.
 * @property p2pAddress The p2p address of the validator.
 * @property primaryAddress The primary address of the validator.
 * @property workerAddress The worker address of the validator.
 * @property nextEpochProtocolPubkeyBytes The protocol public key of the validator for the next
 *   epoch.
 * @property nextEpochProofOfPossession The proof of possession of the validator for the next epoch.
 * @property nextEpochNetworkPubkeyBytes The network public key of the validator for the next epoch.
 * @property nextEpochWorkerPubkeyBytes The worker public key of the validator for the next epoch.
 * @property nextEpochNetAddress The net address of the validator for the next epoch.
 * @property nextEpochP2pAddress The p2p address of the validator for the next epoch.
 * @property nextEpochPrimaryAddress The primary address of the validator for the next epoch.
 * @property nextEpochWorkerAddress The worker address of the validator for the next epoch.
 * @property votingPower The voting power of the validator.
 * @property operationCapId The operation cap ID of the validator.
 * @property gasPrice The gas price of the validator.
 * @property commissionRate The commission rate of the validator.
 * @property nextEpochStake The stake of the validator for the next epoch.
 * @property nextEpochGasPrice The gas price of the validator for the next epoch.
 * @property nextEpochCommissionRate The commission rate of the validator for the next epoch.
 * @property stakingPoolId The staking pool ID of the validator.
 * @property stakingPoolActivationEpoch The staking pool activation epoch of the validator.
 * @property stakingPoolDeactivationEpoch The staking pool deactivation epoch of the validator.
 * @property stakingPoolSuiBalance The SUI balance of the staking pool of the validator.
 * @property rewardsPool The rewards pool of the validator.
 * @property poolTokenBalance The pool token balance of the validator.
 * @property pendingStake The pending stake of the validator.
 * @property pendingTotalSuiWithdraw The pending total SUI withdraw of the validator.
 * @property pendingPoolTokenWithdraw The pending pool token withdraw of the validator.
 * @property exchangeRatesId The exchange rates ID of the validator.
 * @property exchangeRatesSize The exchange rates size of the validator.
 */
@Serializable
data class SuiValidatorSummary(
  val suiAddress: String,
  val protocolPubkeyBytes: String,
  val networkPubkeyBytes: String,
  val workerPubkeyBytes: String,
  val proofOfPossessionBytes: String,
  val name: String,
  val description: String,
  val imageUrl: String,
  val projectUrl: String,
  val netAddress: String,
  val p2pAddress: String,
  val primaryAddress: String,
  val workerAddress: String,
  val nextEpochProtocolPubkeyBytes: String? = null,
  val nextEpochProofOfPossession: String? = null,
  val nextEpochNetworkPubkeyBytes: String? = null,
  val nextEpochWorkerPubkeyBytes: String? = null,
  val nextEpochNetAddress: String? = null,
  val nextEpochP2pAddress: String? = null,
  val nextEpochPrimaryAddress: String? = null,
  val nextEpochWorkerAddress: String? = null,
  val votingPower: Long,
  val operationCapId: String,
  val gasPrice: Long,
  val commissionRate: Long,
  val nextEpochStake: Long,
  val nextEpochGasPrice: Long,
  val nextEpochCommissionRate: Long,
  val stakingPoolId: String,
  val stakingPoolActivationEpoch: Long,
  val stakingPoolDeactivationEpoch: Long? = null,
  val stakingPoolSuiBalance: Long,
  val rewardsPool: Long,
  val poolTokenBalance: Long,
  val pendingStake: Long,
  val pendingTotalSuiWithdraw: Long,
  val pendingPoolTokenWithdraw: Long,
  val exchangeRatesId: String,
  val exchangeRatesSize: Long,
)
