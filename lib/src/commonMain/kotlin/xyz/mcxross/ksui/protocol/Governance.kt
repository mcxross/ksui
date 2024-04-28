package xyz.mcxross.ksui.protocol

import xyz.mcxross.ksui.generated.getcommitteeinfobyepoch.Epoch
import xyz.mcxross.ksui.model.Option

interface Governance {
  suspend fun getCommitteeInfo(epoch: Long? = null): Option<Epoch?>
}
