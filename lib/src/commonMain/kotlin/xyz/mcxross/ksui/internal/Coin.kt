package xyz.mcxross.ksui.internal

import xyz.mcxross.graphql.client.DefaultGraphQLClient
import xyz.mcxross.ksui.generated.GetAddressBalance
import xyz.mcxross.ksui.generated.GetCoinMetadata
import xyz.mcxross.ksui.generated.GetCoinSupply
import xyz.mcxross.ksui.generated.GetCoinsByOwner
import xyz.mcxross.ksui.generated.GetCoinsByTypeAndOwner
import xyz.mcxross.ksui.generated.getaddressbalance.Address
import xyz.mcxross.ksui.generated.getcoinmetadata.CoinMetadata
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiApiType
import xyz.mcxross.ksui.model.SuiConfig

internal suspend fun getAllBalances(config: SuiConfig, address: SuiAddress): Option<Address?> {

  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))

  val request = GetAddressBalance(GetAddressBalance.Variables(address.pubKey))

  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.address)
}

internal suspend fun getAllCoins(
  config: SuiConfig,
  address: SuiAddress,
  type: String,
  limit: Int,
): Option<xyz.mcxross.ksui.generated.getcoinsbytypeandowner.Address?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request =
    GetCoinsByTypeAndOwner(GetCoinsByTypeAndOwner.Variables(address.pubKey, type, limit))
  val response = client.execute<GetCoinsByTypeAndOwner.Result>(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.address)
}

internal suspend fun getCoins(
  config: SuiConfig,
  address: SuiAddress,
  cursor: String? = null,
  limit: Int? = null,
): Option<xyz.mcxross.ksui.generated.getcoinsbyowner.Address?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request = GetCoinsByOwner(GetCoinsByOwner.Variables(address.pubKey, cursor, limit))
  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.address)
}

internal suspend fun getSupply(
  config: SuiConfig,
  type: String,
): Option<xyz.mcxross.ksui.generated.getcoinsupply.CoinMetadata?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request = GetCoinSupply(GetCoinSupply.Variables(type))
  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.coinMetadata)
}

internal suspend fun getBalance(config: SuiConfig, address: SuiAddress): Option<Address?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request = GetAddressBalance(GetAddressBalance.Variables(address.pubKey))
  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.address)
}

internal suspend fun getCoinMetadata(config: SuiConfig, type: String): Option<CoinMetadata?> {
  val client = DefaultGraphQLClient(config.getRequestUrl(SuiApiType.INDEXER))
  val request = GetCoinMetadata(GetCoinMetadata.Variables(type))
  val response = client.execute(request)

  if (response.errors != null) {
    return Option.None
  }

  return Option.Some(response.data!!.coinMetadata)
}
