package xyz.mcxross.ksui.api

import xyz.mcxross.ksui.generated.getcommitteeinfobyepoch.Epoch
import xyz.mcxross.ksui.internal.getCommitteeInfo
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Governance

class Governance(val config: SuiConfig) : Governance {
  override suspend fun getCommitteeInfo(epoch: Long?): Option<Epoch?> =
    getCommitteeInfo(config, epoch)
}
