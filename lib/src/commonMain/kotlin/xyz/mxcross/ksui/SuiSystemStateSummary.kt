package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class SuiSystemStateSummary(
  val activeValidators: List<SuiValidatorSummary>,
  val epoch: Long,
  val epochStartTimestampMs: Long,
  val governanceStartEpoch: Long,
  val maxValidatorCandidateCount: Long,
  val minValidatorStake: Long,
  val protocolVersion: Long,
  val referenceGasPrice: Long,
  val safeMode: Boolean,
  val stakeSubsidyBalance: Long,
  val stakeSubsidyCurrentEpochAmount: Long,
  val stakeSubsidyEpochCounter: Long,
  val storageFund: Long,
  val totalStake: Long,
)
