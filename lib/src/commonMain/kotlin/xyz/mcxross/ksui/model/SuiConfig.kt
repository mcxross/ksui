package xyz.mcxross.ksui.model

import xyz.mcxross.ksui.util.NetworkToFaucetAPI
import xyz.mcxross.ksui.util.NetworkToIndexerAPI
import xyz.mcxross.ksui.util.NetworkToNodeAPI

class SuiConfig(settings: SuiSettings? = null) {

  val network: Network = settings?.network ?: Network.DEVNET
  val fullNode: String? = settings?.fullNode
  val faucet: String? = settings?.faucet
  val indexer: String? = settings?.indexer
  val fullNodeConfig: FullNodeConfig = settings?.fullNodeConfig ?: FullNodeConfig()
  val indexerConfig: IndexerConfig = settings?.indexerConfig ?: IndexerConfig()
  val faucetConfig: FaucetConfig = settings?.faucetConfig ?: FaucetConfig()

  fun getRequestUrl(apiType: SuiApiType): String {
    return when (apiType) {
      SuiApiType.FULLNODE -> {
        fullNode
          ?: if (network == Network.CUSTOM) throw Exception("Please provide a custom full node url")
          else
            NetworkToNodeAPI.getOrElse(network.name.lowercase()) {
              throw Exception("Invalid network")
            }
      }
      SuiApiType.FAUCET -> {
        faucet
          ?: if (network == Network.CUSTOM) throw Exception("Please provide a custom faucet url")
          else
            NetworkToFaucetAPI.getOrElse(network.name.lowercase()) {
              throw Exception("Invalid network")
            }
      }
      SuiApiType.INDEXER -> {
        indexer
          ?: if (network == Network.CUSTOM) throw Exception("Please provide a custom indexer url")
          else
            NetworkToIndexerAPI.getOrElse(network.name.lowercase()) {
              throw Exception("Invalid network")
            }
      }
    }
  }
}

/** General type definition for client headers */
open class ClientHeadersType {
  open var headers: Map<String, Any>? = null
}

/**
 * A Fullnode only configuration object.
 *
 * @param headers - extra headers we want to send with the request
 */
data class FullNodeConfig(override var headers: Map<String, Any>? = null) : ClientHeadersType()

/**
 * An Indexer only configuration object.
 *
 * @param headers - extra headers we want to send with the request
 */
data class IndexerConfig(override var headers: Map<String, Any>? = null) : ClientHeadersType()

/**
 * A Faucet only configuration object
 *
 * @param headers - extra headers we want to send with the request
 * @param authToken - an auth token to send with a faucet request
 */
data class FaucetConfig(val headers: Map<String, Any>? = null, val authToken: String? = null)
