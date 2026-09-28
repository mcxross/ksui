package xyz.mcxross.ksui.ptb

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.FundingSource
import xyz.mcxross.ksui.core.model.Holdings
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.StructTag
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.PtbDsl
import xyz.mcxross.ksui.generated.GetCoinsQuery

/** PTB operations that select funds using a Sui client. [sender] is required for funding calls. */
class NetworkPtbDsl(
  builder: ProgrammableTransactionBuilder,
  private val sui: Sui,
  var sender: AccountAddress? = null,
) : PtbDsl(builder) {
  private val funding = mutableMapOf<String, FundingState>()
  private var fundingOwner: AccountAddress? = null

  /** Produce a Coin<T> from either holding system. Amount is in base units. */
  suspend fun coin(
    amount: ULong,
    coinType: TypeTag = SUI_TYPE,
    source: FundingSource = FundingSource.Auto,
    useGasCoin: Boolean = true,
  ): Argument = fund(amount, coinType, source, useGasCoin, asBalance = false)

  /** Produce a Balance<T> from either holding system. Amount is in base units. */
  suspend fun balance(
    amount: ULong,
    coinType: TypeTag = SUI_TYPE,
    source: FundingSource = FundingSource.Auto,
    useGasCoin: Boolean = true,
  ): Argument = fund(amount, coinType, source, useGasCoin, asBalance = true)

  private suspend fun fund(
    amount: ULong,
    coinType: TypeTag,
    source: FundingSource,
    useGasCoin: Boolean,
    asBalance: Boolean,
  ): Argument {
    if (amount == 0uL) {
      val module = if (asBalance) "balance" else "coin"
      return builder.moveCall("0x2::$module::zero", listOf(coinType))
    }
    val owner = requireNotNull(sender) { "Set sender before requesting transaction funds" }
    require(fundingOwner == null || fundingOwner == owner) {
      "Sender changed after reserving transaction funds"
    }
    val state = state(owner, coinType)
    fundingOwner = owner
    val fromAddress = when (source) {
      FundingSource.CoinObjects -> 0uL
      FundingSource.AddressBalance -> amount
      FundingSource.Auto -> minOf(amount, state.addressRemaining)
    }
    require(fromAddress <= state.addressRemaining) {
      "Insufficient address balance for $coinType: need $fromAddress, have ${state.addressRemaining}"
    }
    val fromObjects = amount - fromAddress
    require(fromObjects <= state.objectRemaining) {
      "Insufficient coin objects for $coinType: need $fromObjects, have ${state.objectRemaining}"
    }

    val objectCoin = if (fromObjects > 0uL) coinFromObjects(
      owner, coinType, fromObjects, state, useGasCoin
    ) else null
    state.addressRemaining -= fromAddress
    state.objectRemaining -= fromObjects

    if (asBalance && objectCoin == null) return builder.withdrawBalance(fromAddress, coinType)
    val addressCoin = if (fromAddress > 0uL) builder.withdrawCoin(fromAddress, coinType) else null
    val resultCoin = when {
      addressCoin == null -> requireNotNull(objectCoin)
      objectCoin == null -> addressCoin
      else -> {
        builder.mergeCoins(addressCoin, listOf(objectCoin))
        addressCoin
      }
    }
    return if (asBalance) {
      builder.moveCall("0x2::coin::into_balance", listOf(coinType), listOf(resultCoin))
    } else resultCoin
  }

  private suspend fun state(owner: AccountAddress, coinType: TypeTag): FundingState {
    val key = coinType.toString()
    funding[key]?.let {
      require(it.owner == owner) { "Sender changed after reserving $key funds" }
      return it
    }
    val holdings = when (val result = sui.getHoldings(owner, key)) {
      is Result.Ok -> result.value
      is Result.Err -> error("Could not fetch holdings for $key: ${result.error}")
    }
    return FundingState(owner, holdings).also { funding[key] = it }
  }

  private suspend fun coinFromObjects(
    owner: AccountAddress,
    coinType: TypeTag,
    amount: ULong,
    state: FundingState,
    useGasCoin: Boolean,
  ): Argument {
    if (coinType == SUI_TYPE && useGasCoin) {
      require(!state.usedExplicitObjects) {
        "Cannot use the SUI gas coin after selecting explicit SUI coin objects"
      }
      state.usedGasCoin = true
      return builder.splitCoins(Argument.GasCoin, listOf(builder.pure(amount))).single()
    }
    require(coinType != SUI_TYPE || !state.usedGasCoin) {
      "Cannot select explicit SUI coin objects after using the gas coin"
    }
    if (!state.loadedCoins) loadCoins(owner, coinType, state)
    val selectableTotal = state.coins.fold(0uL) { total, coin ->
      val next = total + coin.balance
      require(next >= total) { "Coin object total exceeds u64" }
      next
    }
    require(selectableTotal >= amount) {
      "Insufficient selectable coin objects for $coinType: need $amount, found $selectableTotal"
    }
    val selected = mutableListOf<AvailableCoin>()
    var selectedTotal = 0uL
    val singleIndex = state.coins.indices
      .filter { state.coins[it].balance >= amount }
      .minByOrNull { state.coins[it].balance }
    if (singleIndex != null) {
      val coin = state.coins.removeAt(singleIndex)
      selected += coin
      selectedTotal = coin.balance
    } else {
      state.coins.sortByDescending { it.balance }
    }
    while (selectedTotal < amount && state.coins.isNotEmpty()) {
      val next = state.coins.removeAt(0)
      selected += next
      selectedTotal += next.balance
    }
    state.usedExplicitObjects = true
    val arguments = selected.map { it.argument ?: builder.`object`(
      ObjectArg.ImmOrOwnedObject(requireNotNull(it.reference))
    ) }
    val primary = arguments.first()
    if (arguments.size > 1) builder.mergeCoins(primary, arguments.drop(1))
    if (selectedTotal == amount) return primary
    val spend = builder.splitCoins(primary, listOf(builder.pure(amount))).single()
    state.coins.add(0, AvailableCoin(balance = selectedTotal - amount, argument = primary))
    return spend
  }

  private suspend fun loadCoins(owner: AccountAddress, coinType: TypeTag, state: FundingState) {
    var cursor: String? = null
    do {
      val response = when (
        val result = sui.getCoins(
          address = owner,
          cursor = cursor,
          type = "0x2::coin::Coin<$coinType>",
        )
      ) {
        is Result.Ok -> result.value
        is Result.Err -> error("Could not list coin objects for $coinType: ${result.error}")
      }
      val page = requireNotNull(response?.address?.objects) { "Coin object page unavailable" }
      page.nodes.forEach { node ->
        val balance = coinBalance(node)
        val reference = ObjectReference(
          Reference(AccountAddress.fromString(node.address.toString())),
          requireNotNull(node.version).toString().toLong(),
          ObjectDigest(Digest.fromString(requireNotNull(node.digest))),
        )
        state.coins += AvailableCoin(balance = balance, reference = reference)
      }
      val next = page.pageInfo.endCursor
      if (!page.pageInfo.hasNextPage) break
      require(next != null && next != cursor) { "Coin object pagination did not advance" }
      cursor = next
    } while (true)
    state.loadedCoins = true
  }

  private fun coinBalance(node: GetCoinsQuery.Node): ULong {
    val fields = node.contents?.json as? Map<*, *>
      ?: error("Coin object ${node.address} has no JSON contents")
    return requireNotNull(fields["balance"]) { "Coin object ${node.address} has no balance" }
      .toString().toULong()
  }

  private data class AvailableCoin(
    val balance: ULong,
    val reference: ObjectReference? = null,
    val argument: Argument? = null,
  )

  private class FundingState(val owner: AccountAddress, holdings: Holdings) {
    var addressRemaining = holdings.addressBalance
    var objectRemaining = holdings.coinObjects
    val coins = mutableListOf<AvailableCoin>()
    var loadedCoins = false
    var usedGasCoin = false
    var usedExplicitObjects = false
  }
}

private val SUI_TYPE = TypeTag.Struct(StructTag(AccountAddress.fromString("0x2"), "sui", "SUI"))
