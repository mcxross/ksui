package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class Stake(
  val stakedSuiId: String,
  val stakeRequestEpoch: String,
  val stakeActiveEpoch: String,
  val principal: String,
  val status: String,
  val estimatedReward: String = "",
)

@Serializable
data class DelegatedStake(
  val validatorAddress: String,
  val stakingPool: String,
  val stakes: List<Stake>,
)
