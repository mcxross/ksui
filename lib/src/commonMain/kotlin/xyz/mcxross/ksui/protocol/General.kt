package xyz.mcxross.ksui.protocol

import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Option

interface General {
  suspend fun getChainIdentifier() : Option<String>
  suspend fun getReferenceGasPrice() : Option<String?>
  suspend fun getCheckpoint(checkpointId: CheckpointId? = null) : Option<Checkpoint?>
}
