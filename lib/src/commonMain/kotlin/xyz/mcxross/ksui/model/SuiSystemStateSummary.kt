package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import xyz.mcxross.ksui.model.serializer.ValidatorReportRecordSerializer

/**
 * Latest SUI system state object on-chain
 *
 * @param epoch The current epoch ID, starting from 0.
 * @param protocolVersion The current protocol version, starting from 1.
 * @param systemStateVersion The current version of the system state data structure type.
 * @param storageFundTotalObjectStorageRebates The storage rebates of all the objects on-chain
 *   stored in the storage fund.
 * @param storageFundNonRefundableBalance The non-refundable portion of the storage fund coming from
 *   storage reinvestment, non-refundable storage rebates and any leftover staking rewards.
 * @param referenceGasPrice The reference gas price for the current epoch.
 * @param safeMode Whether the system is running in a downgraded safe mode due to a non-recoverable
 *   bug. This is set whenever Sui failed to execute advance_epoch, and ended up executing
 *   advance_epoch_safe_mode. It can be reset once Sui is able to successfully execute
 *   advance_epoch.
 * @param safeModeStorageRewards Amount of storage rewards accumulated (and not yet distributed)
 *   during safe mode.
 * @param safeModeComputationRewards Amount of computation rewards accumulated (and not yet
 *   distributed) during safe mode.
 * @param safeModeStorageRebates Amount of storage rebates accumulated (and not yet burned) during
 *   safe mode.
 * @param safeModeNonRefundableStorageFee Amount of non-refundable storage fee accumulated during
 *   safe mode.
 * @param epochStartTimestampMs Unix timestamp of the current epoch start.
 * @param epochDurationMs The duration of an epoch, in milliseconds.
 * @param stakeSubsidyStartEpoch The starting epoch in which stake subsidies start being paid out.
 * @param maxValidatorCount Maximum number of active validators at any moment. Sui does not allow
 *   the number of validators in any epoch to go above this.
 * @param minValidatorJoiningStake Lower-bound on the amount of stake required to become a
 *   validator.
 * @param validatorLowStakeThreshold Validators with stake amount below
 *   `validator_low_stake_threshold` are considered to have low stake and will be escorted out of
 *   the validator set after being below this threshold for more than
 *   `validator_low_stake_grace_period` number of epochs.
 * @param validatorVeryLowStakeThreshold Validators with stake below
 *   `validator_very_low_stake_threshold` will be removed immediately at epoch change, no grace
 *   period.
 * @param validatorLowStakeGracePeriod A validator can have stake below
 *   `validator_low_stake_threshold` for this many epochs before being kicked out.
 * @param stakeSubsidyBalance Balance of SUI set aside for stake subsidies that will be drawn down
 *   over time.
 * @param stakeSubsidyDistributionCounter This counter may be different from the current epoch
 *   number if in some epochs we decide to skip the subsidy.
 * @param stakeSubsidyCurrentDistributionAmount The amount of stake subsidy to be drawn down per
 *   epoch. This amount decays and decreases over time.
 * @param stakeSubsidyPeriodLength Number of distributions to occur before the distribution amount
 *   decays.
 * @param stakeSubsidyDecreaseRate The rate at which the distribution amount decays at the end of
 *   each period. Expressed in basis points.
 * @param totalStake Total amount of stake from all active validators at the beginning of the epoch.
 * @param activeValidators The list of active validators in the current epoch.
 * @param pendingActiveValidatorsId ID of the object that contains the list of new validators that
 *   will join at the end of the epoch.
 * @param pendingActiveValidatorsSize Number of new validators that will join at the end of the
 *   epoch.
 * @param pendingRemovals Removal requests from the validators. Each element is an index pointing to
 *   `active_validators`.
 * @param stakingPoolMappingsId ID of the object that maps from staking pool's ID to the sui address
 *   of a validator.
 * @param stakingPoolMappingsSize Number of staking pool mappings.
 * @param inactivePoolsId ID of the object that maps from a staking pool ID to the inactive
 *   validator that has that pool as its staking pool.
 * @param inactivePoolsSize Number of inactive staking pools.
 * @param validatorCandidatesId ID of the object that stores pre-active validators, mapping their
 *   addresses to their `Validator` structs.
 * @param validatorCandidatesSize Number of pre-active validators.
 * @param validatorReportRecords A map storing the records of validator reporting each other.
 */
@Serializable
data class SuiSystemStateSummary(
  val epoch: Long,
  val protocolVersion: Long,
  val systemStateVersion: Long,
  val storageFundTotalObjectStorageRebates: Long,
  val storageFundNonRefundableBalance: Long,
  val referenceGasPrice: Long,
  val safeMode: Boolean,
  val safeModeStorageRewards: Long,
  val safeModeComputationRewards: Long,
  val safeModeStorageRebates: Long,
  val safeModeNonRefundableStorageFee: Long,
  val epochStartTimestampMs: Long,
  val epochDurationMs: Long,
  val stakeSubsidyStartEpoch: Long,
  val maxValidatorCount: Int,
  val minValidatorJoiningStake: Long,
  val validatorLowStakeThreshold: Long,
  val validatorVeryLowStakeThreshold: Long,
  val validatorLowStakeGracePeriod: Long,
  @Transient val stakeSubsidyBalance: Long = 9_223_372_036_854_775_807,
  val stakeSubsidyDistributionCounter: Long,
  val stakeSubsidyCurrentDistributionAmount: Long,
  val stakeSubsidyPeriodLength: Long,
  val stakeSubsidyDecreaseRate: Long,
  val totalStake: Long,
  val activeValidators: List<SuiValidatorSummary>,
  val pendingActiveValidatorsId: String,
  val pendingActiveValidatorsSize: Long,
  val pendingRemovals: List<String>,
  val stakingPoolMappingsId: String,
  val stakingPoolMappingsSize: Long,
  val inactivePoolsId: String,
  val inactivePoolsSize: Int,
  val validatorCandidatesId: String,
  val validatorCandidatesSize: Long,
  val atRiskValidators: List<String>,
  @Serializable(with = ValidatorReportRecordSerializer::class)
  val validatorReportRecords: List<ValidatorReportRecord>,
)
