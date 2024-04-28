package xyz.mcxross.ksui.internal

import xyz.mcxross.graphql.client.DefaultGraphQLClient
import xyz.mcxross.ksui.generated.GetCommitteeInfoByEpoch
import xyz.mcxross.ksui.generated.getcommitteeinfobyepoch.Epoch
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getCommitteeInfo(
  config: SuiConfig,
  epoch: Long?,
): Option<Epoch?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request = GetCommitteeInfoByEpoch(GetCommitteeInfoByEpoch.Variables(epoch?.toInt()))
  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.epoch)
}
