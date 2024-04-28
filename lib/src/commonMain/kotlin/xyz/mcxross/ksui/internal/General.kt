package xyz.mcxross.ksui.internal

import xyz.mcxross.graphql.client.DefaultGraphQLClient
import xyz.mcxross.ksui.generated.GetChainIdentifier
import xyz.mcxross.ksui.generated.GetCheckpoint
import xyz.mcxross.ksui.generated.GetReferenceGasPrice
import xyz.mcxross.ksui.generated.getcheckpoint.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getChainIdentifier(config: SuiConfig): Option<String> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response = client.execute(GetChainIdentifier())

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.chainIdentifier)
}

internal suspend fun getReferenceGasPrice(config: SuiConfig): Option<String?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response = client.execute<GetReferenceGasPrice.Result>(GetReferenceGasPrice())

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.epoch?.referenceGasPrice)
}

internal suspend fun getCheckpoint(
  config: SuiConfig,
  checkpoint: CheckpointId?,
): Option<Checkpoint?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val response =
    client.execute<GetCheckpoint.Result>(
      GetCheckpoint(
        GetCheckpoint.Variables(checkpoint?.digest, checkpoint?.sequenceNumber?.toInt())
      )
    )

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.checkpoint)
}
