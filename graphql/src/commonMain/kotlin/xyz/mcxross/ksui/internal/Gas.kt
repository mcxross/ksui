/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.internal

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import xyz.mcxross.ksui.client.getGraphqlClient
import xyz.mcxross.ksui.core.exception.SuiException
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CoinObject
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.ExecuteTransactionBlockResponseOptions
import xyz.mcxross.ksui.core.model.GasContext
import xyz.mcxross.ksui.core.model.GasCostSummary
import xyz.mcxross.ksui.core.model.GasEnvironment
import xyz.mcxross.ksui.core.model.Holdings
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.model.selectTopGasCoins
import xyz.mcxross.ksui.core.util.MAX_GAS_PAYMENT_OBJECTS
import xyz.mcxross.ksui.core.util.bcsEncode
import xyz.mcxross.ksui.generated.GetGasContextQuery
import xyz.mcxross.ksui.generated.type.ExecutionStatus

private const val MAX_GAS_COIN_PAGES = 20

// Address.objects allows at most 50 per page and defaults to 20 (mainnet/testnet serviceConfig).
internal const val COIN_PAGE_SIZE = 50

internal class GraphqlGasEnvironment(private val config: SuiConfig) : GasEnvironment {

  override suspend fun context(owner: AccountAddress): GasContext {
    val data =
      when (
        val result =
          handleQuery { getGraphqlClient(config).query(GetGasContextQuery(owner.toString())) }
            .toResult()
      ) {
        is Result.Ok -> result.value
        is Result.Err -> throw SuiException("Failed to get gas context: ${result.error}")
      } ?: throw SuiException("Failed to get gas context")
    val epoch = data.epoch ?: throw SuiException("Failed to get the current epoch")
    val balance = data.address?.balance
    return GasContext(
      epoch = epoch.epochId.toString().toULong(),
      referenceGasPrice = requireNotNull(epoch.referenceGasPrice).toString().toULong(),
      chain = Digest.fromString(data.chainIdentifier),
      holdings =
        Holdings(
          coinType = balance?.coinType?.repr ?: "0x2::sui::SUI",
          coinObjects = balance?.coinBalance?.toString()?.toULong() ?: 0UL,
          addressBalance = balance?.addressBalance?.toString()?.toULong() ?: 0UL,
        ),
    )
  }

  override suspend fun coins(owner: AccountAddress): List<CoinObject> {
    val coins = mutableListOf<CoinObject>()
    var cursor: String? = null
    var pages = 0
    do {
      val page =
        when (val result = getCoins(config, owner, first = COIN_PAGE_SIZE, cursor = cursor)) {
          is Result.Ok -> result.value?.address?.objects
          is Result.Err -> throw SuiException("Failed to get gas coins: ${result.error}")
        } ?: throw SuiException("Failed to get gas coins")
      val pageCoins =
        page.nodes.map { node ->
          val balance =
            (node.contents?.json as? Map<*, *>)?.get("balance")
              ?: throw SuiException("Coin object ${node.address} has no balance")
          CoinObject(
            ObjectReference(
              Reference(AccountAddress.fromString(node.address.toString())),
              requireNotNull(node.version).toString().toLong(),
              ObjectDigest(Digest.fromString(requireNotNull(node.digest))),
            ),
            balance.toString().toULong(),
          )
        }
      selectTopGasCoins(coins, pageCoins, MAX_GAS_PAYMENT_OBJECTS)
      pages++
      val next = page.pageInfo.endCursor
      if (!page.pageInfo.hasNextPage || pages >= MAX_GAS_COIN_PAGES) break
      if (next == null || next == cursor) throw SuiException("Gas coin pagination did not advance")
      cursor = next
    } while (true)
    return coins
  }

  @OptIn(ExperimentalEncodingApi::class)
  override suspend fun simulate(transaction: TransactionData): GasCostSummary {
    val needsServerGasSelection =
      transaction is TransactionData.V1 && transaction.gasData.payment.isEmpty()
    val effects =
      when (
        val result =
          dryRunTransactionBlock(
            config,
            Base64.encode(bcsEncode(transaction)),
            ExecuteTransactionBlockResponseOptions(),
            doGasSelection = if (needsServerGasSelection) true else null,
          )
      ) {
        is Result.Ok -> result.value?.simulateTransaction?.effects
        is Result.Err -> throw SuiException("Failed to simulate transaction: ${result.error}")
      } ?: throw SuiException("Simulation returned no effects")
    if (effects.status != ExecutionStatus.SUCCESS) {
      throw SuiException("Transaction simulation failed: ${effects.executionError?.message}")
    }
    val summary =
      effects.gasEffects?.gasSummary ?: throw SuiException("Simulation returned no gas summary")
    return GasCostSummary(
      computationCost = summary.computationCost.toString().toULong(),
      storageCost = summary.storageCost.toString().toULong(),
      storageRebate = summary.storageRebate.toString().toULong(),
    )
  }
}
