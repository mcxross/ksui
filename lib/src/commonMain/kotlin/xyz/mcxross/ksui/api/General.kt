package xyz.mcxross.ksui.api

import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.General

class General(val config: SuiConfig) : General {
  override suspend fun getChainIdentifier(): Option<String> =
    xyz.mcxross.ksui.internal.getChainIdentifier(config)

  override suspend fun getReferenceGasPrice(): Option<String?> =
    xyz.mcxross.ksui.internal.getReferenceGasPrice(config)

  override suspend fun getCheckpoint(checkpointId: CheckpointId?): Option<Checkpoint?> = xyz.mcxross.ksui.internal.getCheckpoint(config, checkpointId)
}
