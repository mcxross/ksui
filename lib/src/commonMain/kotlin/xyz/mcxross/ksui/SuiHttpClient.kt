package xyz.mcxross.ksui

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import org.gciatto.kt.math.BigInteger

/** A Kotlin wrapper around the Sui JSON-RPC API for interacting with a Sui full node. */
class SuiHttpClient constructor(private val configContainer: ConfigContainer) :
  SuiClient {

  private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
  }

  /**
   * Calls a Sui RPC method with the given name and parameters.
   *
   * @param method The name of the Sui RPC method to call.
   * @param params The parameters to pass to the Sui RPC method.
   * @return The result of the Sui RPC method, or null if there was an error.
   */
  private suspend fun call(method: String, vararg params: Any): HttpResponse {
    val requestBody = buildJsonObject {
      put("jsonrpc", "2.0")
      put("id", 1)
      put("method", method)
      putJsonArray("params") { for (element in params) add(element.toString()) }
    }
    val response: HttpResponse =
      configContainer.httpClient.post {
        when (configContainer.endPoint) {
          EndPoint.CUSTOM -> {
            url(configContainer.customUrl)
          }
          EndPoint.DEVNET -> {
            url("https://fullnode.devnet.sui.io:443")
          }
          EndPoint.TESTNET -> {
            url("https://fullnode.testnet.sui.io:443")
          }
          EndPoint.MAINNET -> {
            url("https://fullnode.sui.io:443")
          }
        }
        contentType(ContentType.Application.Json)
        setBody(requestBody.toString())
      }

    return response
  }

  /**
   * Create an unsigned batched transaction.
   *
   * @param signer transaction signer's Sui address
   * @param singleTransactionParams list of transaction request parameters
   * @param gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided
   * @param gasBudget the gas budget, the transaction will fail if the gas cost exceed the budget
   * @param txnBuilderMode whether this is a regular transaction or a Dev Inspect Transaction
   * @return [TransactionBytes]
   */
  suspend fun batchTransaction(
    signer: SuiAddress,
    singleTransactionParams: List<RPCTransactionRequestParams>,
    gas: Gas? = null,
    gasBudget: Int,
    txnBuilderMode: SuiTransactionBuilderMode = SuiTransactionBuilderMode.REGULAR
  ): TransactionBytes =
    Json.decodeFromString(
      serializer(),
      when (gas) {
        null ->
          call(
            "sui_batchTransaction",
            signer.pubKey,
            singleTransactionParams,
            gasBudget,
            when (txnBuilderMode) {
              SuiTransactionBuilderMode.REGULAR -> "Commit"
              SuiTransactionBuilderMode.DEV_INSPECT -> ""
            },
          )
        else ->
          call(
            "sui_batchTransaction",
            signer.pubKey,
            singleTransactionParams,
            gas,
            gasBudget,
            when (txnBuilderMode) {
              SuiTransactionBuilderMode.REGULAR -> "Commit"
              SuiTransactionBuilderMode.DEV_INSPECT -> ""
            },
          )
      }.bodyAsText(),
    )

  suspend fun devInspectTransaction(
    senderAddress: SuiAddress,
    txBytes: String,
    gasPrice: BigInteger = BigInteger.of(0),
    epoch: BigInteger? = null
  ): DevInspectResults =
    Json.decodeFromString(
      serializer(),
      when (epoch) {
        null -> call("sui_devInspectTransaction", senderAddress.pubKey, txBytes, gasPrice)
        else -> call("sui_devInspectTransaction", senderAddress.pubKey, txBytes, gasPrice, epoch)
      }.bodyAsText(),
    )

  suspend fun dryRunTransaction(txBytes: BigInteger): DryRunTransactionResponse =
    Json.decodeFromString(serializer(), call("sui_dryRunTransaction", txBytes).bodyAsText())

  suspend fun executeTransaction(
    txBytes: BigInteger,
    signature: BigInteger,
    requestType: ExecuteTransactionRequestType
  ): SuiTransactionResponse =
    Json.decodeFromString(
      serializer(),
      call("sui_executeTransaction", txBytes, signature, requestType).bodyAsText(),
    )

  suspend fun executeTransactionSerializedSig() {}

  /**
   * Return the total coin balance for all coin type, owned by the address owner.
   *
   * @param owner's Sui address.
   * @return [Balance]
   */
  suspend fun getAllBalances(owner: SuiAddress): Balance {
    val result =
      json.decodeFromString<BalanceResult>(
        serializer(),
        call("sui_getBalance", owner.pubKey).bodyAsText()
      )
    return Balance(
      result.value.coinType,
      result.value.coinObjectCount,
      result.value.totalBalance,
      result.value.lockedBalance
    )
  }

  suspend fun getAllCoins() {}

  /**
   * Return the total coin balance for one coin type, owned by the address owner.
   *
   * @param owner's Sui address.
   * @param coinType names for the coin. Defaults to "0x2::sui::SUI"
   * @return [Balance]
   */
  suspend fun getBalance(owner: SuiAddress, coinType: String = "0x2::sui::SUI"): Balance {
    val result =
      json.decodeFromString<BalanceResult>(
        serializer(),
        call("sui_getBalance", *listOf(owner.pubKey, coinType).toTypedArray()).bodyAsText()
      )
    return Balance(
      result.value.coinType,
      result.value.coinObjectCount,
      result.value.totalBalance,
      result.value.lockedBalance
    )
  }

  suspend fun getCheckpoint(id: CheckpointId): Checkpoint {
    return Json.decodeFromString(serializer(), call("sui_getCheckpoint", id).bodyAsText())
  }
  suspend fun getCheckpointContents() {}
  suspend fun getCheckpointContentsByDigest() {}
  suspend fun getCheckpointSummary() {}
  suspend fun getCheckpointSummaryByDigest(digest: CheckpointDigest) {
    return Json.decodeFromString(
      serializer(),
      call("sui_getCheckpointSummaryByDigest", digest).bodyAsText()
    )
  }

  /**
   * Return metadata(e.g., symbol, decimals) for a coin
   *
   * @param coinType name for the coin.
   * @return [SuiCoinMetadata]
   */
  suspend fun getCoinMetadata(coinType: String): SuiCoinMetadata {
    val result =
      json.decodeFromString<SuiCoinMetadataResult>(
        serializer(),
        call("sui_getCoinMetadata", *listOf(coinType).toTypedArray()).bodyAsText()
      )
    return SuiCoinMetadata(
      result.value.decimals,
      result.value.description,
      result.value.iconUrl,
      result.value.id,
      result.value.name,
      result.value.symbol
    )
  }
  suspend fun getCoins() {}
  suspend fun getCommitteeInfo(epoch: Long? = null): SuiCommittee =
    json.decodeFromString(
      serializer(),
      when (epoch) {
        null -> call("sui_getCommitteeInfo").bodyAsText()
        else -> call("sui_getCommitteeInfo", epoch).bodyAsText()
      }
    )

  suspend fun getDelegatedStakes() {}
  suspend fun getDynamicFieldObject() {}
  suspend fun getDynamicFields() {}
  suspend fun getEvents() {}

  /**
   * Return the sequence number of the latest checkpoint that has been executed
   *
   * @return [CheckpointSequenceNumber]
   */
  suspend fun getLatestCheckpointSequenceNumber(): CheckpointSequenceNumber =
    json.decodeFromString(serializer(), call("sui_getLatestCheckpointSequenceNumber").bodyAsText())

  suspend fun getMoveFunctionArgTypes() {}
  suspend fun getNormalizedMoveFunction() {}
  suspend fun getNormalizedMoveModule() {}
  suspend fun getNormalizedMoveModulesByPackage() {}
  suspend fun getNormalizedMoveStruct() {}
  suspend fun getObject() {}

  /**
   * Return the list of objects owned by an address.
   *
   * @param address of the owner
   * @return List<[SuiObjectInfo]>
   */
  suspend fun getObjectsOwnedByAddress(address: SuiAddress): List<SuiObjectInfo> {
    val result =
      json.decodeFromString<SuiObjectResult>(
        serializer(),
        call("sui_getObjectsOwnedByAddress", *listOf(address.pubKey).toTypedArray()).bodyAsText()
      )
    return result.value.map {
      SuiObjectInfo(
        it.objectId,
        it.version,
        Digest(it.digest),
        it.type,
        it.owner,
        Transaction(it.previousTransaction)
      )
    }
  }

  /**
   * Return the reference gas price for the network.
   *
   * @return [GasPrice]
   */
  suspend fun getReferenceGasPrice(): GasPrice =
    json.decodeFromString(serializer(), call("sui_getReferenceGasPrice").bodyAsText())

  suspend fun getSuiSystemState() {}
  suspend fun getTotalSupply(coinType: String): Supply {
    val supplyRaw =
      json.decodeFromString<SupplyRaw>(
        serializer(),
        call("sui_getTotalSupply", coinType).bodyAsText()
      )
    return Supply(value = supplyRaw.valueRaw.value)
  }

  /**
   * Return the total number of transactions known to the server.
   *
   * @return [Long]
   */
  suspend fun getTotalTransactionNumber(): Long =
    json
      .decodeFromString<TransactionNumber>(
        serializer(),
        call("sui_getTotalTransactionNumber").bodyAsText()
      )
      .value

  suspend fun getTransaction(digest: TransactionDigest): SuiTransactionResponse =
    Json.decodeFromString(
      serializer(),
      call("sui_getTransaction", digest).bodyAsText(),
    )

  suspend fun getTransactions(
    query: TransactionQuery,
    cursor: TransactionDigest,
    limit: Long,
    descendingOrder: Boolean = false
  ): TransactionsPage =
    Json.decodeFromString(
      serializer(),
      call("sui_getTransactions", query, cursor, limit, descendingOrder).bodyAsText()
    )
  suspend fun getTransactionsInRange(start: Long, end: Long): TransactionDigests =
    json.decodeFromString(serializer(), call("sui_getTransactionsInRange", start, end).bodyAsText())
  suspend fun getValidators(): Validators =
    json.decodeFromString(serializer(), call("sui_getValidators").bodyAsText())

  suspend fun mergeCoins(
    signer: SuiAddress,
    primaryCoin: ObjectID,
    coinToMerge: ObjectID,
    gas: Gas,
    gasBudget: Long
  ): TransactionBytes =
    Json.decodeFromString(
      serializer(),
      call("sui_mergeCoins", signer, primaryCoin, coinToMerge, gas, gasBudget).bodyAsText()
    )
  suspend fun moveCall(
    signer: SuiAddress,
    packageObjectId: ObjectID,
    module: String,
    function: String,
    typeArguments: TypeTag,
    arguments: List<SuiJsonValue>,
    gas: Gas,
    gasBudget: Long,
    executionMode: SuiTransactionBuilderMode = SuiTransactionBuilderMode.REGULAR
  ): TransactionBytes {
    return Json.decodeFromString(
      serializer(),
      call(
          "sui_moveCall",
          signer,
          packageObjectId,
          module,
          function,
          typeArguments,
          arguments,
          gas,
          gasBudget,
          executionMode
        )
        .bodyAsText()
    )
  }
  suspend fun multiGetTransactions() {}
  suspend fun pay() {}
  suspend fun payAllSui() {}
  suspend fun paySui() {}
  suspend fun publish() {}
  suspend fun requestAddDelegation() {}
  suspend fun requestWithdrawDelegation() {}
  suspend fun splitCoin() {}
  suspend fun splitCoinEqual() {}
  suspend fun submitTransaction() {}
  suspend fun subscribeEvent() {}
  suspend fun tblsSignRandomnessObject() {}
  suspend fun transferObject() {}
  suspend fun transferSui() {}
  suspend fun tryGetPastObject() {}
}
