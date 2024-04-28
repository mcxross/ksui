package xyz.mcxross.ksui.protocol

import xyz.mcxross.ksui.generated.getaddressbalance.Address
import xyz.mcxross.ksui.generated.getcoinmetadata.CoinMetadata
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.SuiAddress

interface Coin {
  suspend fun getAllBalances(address: SuiAddress): Option<Address?>

  suspend fun getAllCoins(address: SuiAddress, type: String, limit: Int): Option<xyz.mcxross.ksui.generated.getcoinsbytypeandowner.Address?>

  suspend fun getCoins(
    address: SuiAddress,
    cursor: String? = null,
    limit: Int? = null,
  ): Option<xyz.mcxross.ksui.generated.getcoinsbyowner.Address?>

  suspend fun getSupply(
    type: String
  ): Option<xyz.mcxross.ksui.generated.getcoinsupply.CoinMetadata?>

  suspend fun getBalance(address: SuiAddress): Option<Address?>

  suspend fun getCoinMetadata(type: String): Option<CoinMetadata?>
}
