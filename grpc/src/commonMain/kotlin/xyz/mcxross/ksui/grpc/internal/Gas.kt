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
import xyz.mcxross.ksui.core.model.selectTopGasCoins
import xyz.mcxross.ksui.core.util.MAX_GAS_PAYMENT_OBJECTS
import xyz.mcxross.ksui.core.util.bcsEncode

private const val MAX_GAS_COIN_PAGES = 20

// ListOwnedObjects returns 50 per page by default and at most 1000 (state_service.proto).
private const val GAS_COIN_PAGE_SIZE = 1000u

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
    var pages = 0
    do {
      val page =
        listOwnedObjects(
          runtime,
          owner,
          pageSize = GAS_COIN_PAGE_SIZE,
          pageToken = pageToken,
          readMask = readMask("object_id", "version", "digest", "balance"),
          objectType = "0x2::coin::Coin<$DEFAULT_SUI_COIN_TYPE>",
        )
      val pageCoins =
        page.objects.map { coin ->
          CoinObject(
            ObjectReference(
              Reference(AccountAddress.fromString(requireNotNull(coin.objectId))),
              requireNotNull(coin.version).toLong(),
              ObjectDigest(Digest(requireNotNull(coin.digest))),
            ),
            requireNotNull(coin.balance),
          )
        }
      selectTopGasCoins(coins, pageCoins, MAX_GAS_PAYMENT_OBJECTS)
      pages++
      val next = page.nextPageToken?.takeIf { it.size > 0 }
      if (next != null && next == pageToken) {
        throw SuiException("gRPC gas coin pagination did not advance")
      }
      pageToken = next
    } while (pageToken != null && pages < MAX_GAS_COIN_PAGES)
    return coins
  }

  override suspend fun simulate(transaction: TransactionData): GasCostSummary {
    val needsServerGasSelection =
      transaction is TransactionData.V1 && transaction.gasData.payment.isEmpty()
    val effects =
      simulateTransaction(
          runtime,
          bcsEncode(transaction),
          readMask("transaction.effects.status", "transaction.effects.gas_used"),
          doGasSelection = if (needsServerGasSelection) true else null,
        )
        .transaction
        .effects
    if (effects.status.success != true) {
      throw SuiException("Transaction simulation failed: ${effects.status.error.description}")
    }
    val gasUsed = effects.gasUsed
    return GasCostSummary(
      // A missing field would otherwise budget only the safety overhead and fail on chain.
      computationCost =
        gasUsed.computationCost ?: throw SuiException("Simulation returned no computation cost"),
      storageCost =
        gasUsed.storageCost ?: throw SuiException("Simulation returned no storage cost"),
      storageRebate =
        gasUsed.storageRebate ?: throw SuiException("Simulation returned no storage rebate"),
    )
  }

  private fun readMask(vararg paths: String): FieldMask =
    FieldMaskInternal().apply { this.paths = paths.toList() }
}
