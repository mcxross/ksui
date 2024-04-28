package xyz.mcxross.ksui.api

import xyz.mcxross.ksui.generated.getaddressbalance.Address
import xyz.mcxross.ksui.generated.getcoinmetadata.CoinMetadata
import xyz.mcxross.ksui.internal.getAllBalances
import xyz.mcxross.ksui.internal.getBalance
import xyz.mcxross.ksui.internal.getCoinMetadata
import xyz.mcxross.ksui.internal.getSupply
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Coin

class Coin(val config: SuiConfig) : Coin {
  override suspend fun getAllBalances(address: SuiAddress): Option<Address?> =
    getAllBalances(config, address)

  override suspend fun getAllCoins(address: SuiAddress, type: String, limit: Int): Option<xyz.mcxross.ksui.generated.getcoinsbytypeandowner.Address?> =
    xyz.mcxross.ksui.internal.getAllCoins(config, address, type, limit)

  override suspend fun getCoins(
    address: SuiAddress,
    cursor: String?,
    limit: Int?,
  ): Option<xyz.mcxross.ksui.generated.getcoinsbyowner.Address?> =
    xyz.mcxross.ksui.internal.getCoins(config, address, cursor, limit)

  override suspend fun getSupply(
    type: String
  ): Option<xyz.mcxross.ksui.generated.getcoinsupply.CoinMetadata?> = getSupply(config, type)

  override suspend fun getBalance(address: SuiAddress): Option<Address?> =
    getBalance(config, address)

  override suspend fun getCoinMetadata(type: String): Option<CoinMetadata?> =
    getCoinMetadata(config, type)
}
