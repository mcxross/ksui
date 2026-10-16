package xyz.mcxross.ksui.grpc.internal

import com.google.protobuf.kotlin.FieldMask
import com.google.protobuf.kotlin.FieldMaskInternal
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.io.bytestring.ByteString
import xyz.mcxross.ksui.core.exception.SuiException
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CoinObject
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.GasContext
import xyz.mcxross.ksui.core.model.GasCostSummary
import xyz.mcxross.ksui.core.model.GasEnvironment
import xyz.mcxross.ksui.core.model.Holdings
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.util.MAX_GAS_PAYMENT_OBJECTS
import xyz.mcxross.ksui.core.util.bcsEncode

internal class GrpcGasEnvironment(private val runtime: GrpcRuntime) : GasEnvironment {

  override suspend fun context(owner: AccountAddress): GasContext = coroutineScope {
    val epochResponse = async {
      getEpoch(runtime, readMask = readMask("epoch", "reference_gas_price"))
    }
    val serviceInfo = async { getServiceInfo(runtime) }
    val balanceResponse = async { getBalance(runtime, owner) }

    val epoch = epochResponse.await().epoch
    val chainId =
      serviceInfo.await().chainId ?: throw SuiException("Failed to get the chain identifier")
    val balance = balanceResponse.await().balance
    GasContext(
      epoch = epoch.epoch ?: throw SuiException("Failed to get the current epoch"),
      referenceGasPrice =
        epoch.referenceGasPrice ?: throw SuiException("Failed to get the reference gas price"),
      chain = Digest.fromString(chainId),
      holdings =
        Holdings(
          coinType = balance.coinType ?: DEFAULT_SUI_COIN_TYPE,
          coinObjects = balance.coinBalance ?: 0UL,
          addressBalance = balance.addressBalance ?: 0UL,
        ),
    )
  }

  override suspend fun coins(owner: AccountAddress): List<CoinObject> {
    val coins = mutableListOf<CoinObject>()
    var pageToken: ByteString? = null
    do {
      val page =
        listOwnedObjects(
          runtime,
          owner,
          pageToken = pageToken,
          readMask = readMask("object_id", "version", "digest", "balance"),
          objectType = "0x2::coin::Coin<$DEFAULT_SUI_COIN_TYPE>",
        )
      page.objects.mapTo(coins) { coin ->
        CoinObject(
          ObjectReference(
            Reference(AccountAddress.fromString(requireNotNull(coin.objectId))),
            requireNotNull(coin.version).toLong(),
            ObjectDigest(Digest(requireNotNull(coin.digest))),
          ),
          requireNotNull(coin.balance),
        )
      }
      pageToken = page.nextPageToken?.takeIf { it.size > 0 }
    } while (pageToken != null && coins.size < MAX_GAS_PAYMENT_OBJECTS)
    return coins
  }

  override suspend fun simulate(transaction: TransactionData): GasCostSummary {
    val effects =
      simulateTransaction(
          runtime,
          bcsEncode(transaction),
          readMask("transaction.effects.status", "transaction.effects.gas_used"),
        )
        .transaction
        .effects
    if (effects.status.success != true) {
      throw SuiException("Transaction simulation failed: ${effects.status.error.description}")
    }
    val gasUsed = effects.gasUsed
    return GasCostSummary(
      computationCost = gasUsed.computationCost ?: 0UL,
      storageCost = gasUsed.storageCost ?: 0UL,
      storageRebate = gasUsed.storageRebate ?: 0UL,
    )
  }

  private fun readMask(vararg paths: String): FieldMask =
    FieldMaskInternal().apply { this.paths = paths.toList() }
}
