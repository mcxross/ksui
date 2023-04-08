package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.SuiCommitteeValidatorSerializer

@Serializable
abstract class CommitteeInfo {
  abstract val epoch: Long
  @Serializable
  data class SuiCommittee(
      override val epoch: Long,
      @Serializable(with = SuiCommitteeValidatorSerializer::class)
      val validators: List<Validator>,
  ) : CommitteeInfo()
}
