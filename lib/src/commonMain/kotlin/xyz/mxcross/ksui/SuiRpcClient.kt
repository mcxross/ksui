package xyz.mxcross.ksui

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType


/**
 * A Kotlin wrapper around the Sui JSON-RPC API for interacting with a Sui full node.
 */
class SuiRpcClient private constructor(
    private val endpoint: EndPoint = EndPoint.DEVNET,
    private val httpClient: HttpClient = HttpClient(CIO)
) {

    /**
     * Calls a Sui RPC method with the given name and parameters.
     *
     * @param method The name of the Sui RPC method to call.
     * @param params The parameters to pass to the Sui RPC method.
     *
     * @return The result of the Sui RPC method, or null if there was an error.
     */
    private suspend fun call(method: String, vararg params: Any): Any {
        val requestBody = mapOf(
            "jsonrpc" to "2.0",
            "id" to 1,
            "method" to method,
            "params" to params.toList()
        )

        val response: HttpResponse = httpClient.post {
            url("endpoint")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        return response
    }

    /**
     * Create an unsigned batched transaction.
     *
     * @param signer transaction signer's Sui address
     * @param singleTransactionParams list of transaction request parameters
     * @param gas object to be used in this transaction, node will pick one from the signer's possession if not provided
     * @param gasBudget the gas budget, the transaction will fail if the gas cost exceed the budget
     * @param txnBuilderMode whether this is a regular transaction or a Dev Inspect Transaction
     *
     * @return [TransactionBytes]
     */
    fun batchTransaction(
        signer: SuiAddress,
        singleTransactionParams: List<RPCTransactionRequestParams>,
        gas: Gas? = null,
        gasBudget: Int,
        txnBuilderMode: SuiTransactionBuilderMode = SuiTransactionBuilderMode.REGULAR
    ): TransactionBytes {
        return TransactionBytes()
    }

    suspend fun devInspectTransaction() {}
    suspend fun dryRunTransaction() {}
    suspend fun executeTransaction() {}
    suspend fun executeTransactionSerializedSig() {}
    suspend fun getAllBalances() {}
    suspend fun getAllCoins() {}
    suspend fun getBalance() {}
    suspend fun getCheckpoint() {}
    suspend fun getCheckpointContents() {}
    suspend fun getCheckpointContentsByDigest() {}
    suspend fun getCheckpointSummary() {}
    suspend fun getCheckpointSummaryByDigest() {}
    suspend fun getCoinMetadata() {}
    suspend fun getCoins() {}
    suspend fun getCommitteeInfo() {}
    suspend fun getDelegatedStakes() {}
    suspend fun getDynamicFieldObject() {}
    suspend fun getDynamicFields() {}
    suspend fun getEvents() {}
    suspend fun getLatestCheckpointSequenceNumber() {}
    suspend fun getMoveFunctionArgTypes() {}
    suspend fun getNormalizedMoveFunction() {}
    suspend fun getNormalizedMoveModule() {}
    suspend fun getNormalizedMoveModulesByPackage() {}
    suspend fun getNormalizedMoveStruct() {}
    suspend fun getObject() {}
    suspend fun getObjectsOwnedByAddress() {}
    suspend fun getReferenceGasPrice() {}
    suspend fun getSuiSystemState() {}
    suspend fun getTotalSupply() {}
    suspend fun getTotalTransactionNumber() {}
    suspend fun getTransaction() {}
    suspend fun getTransactions() {}
    suspend fun getTransactionsInRange() {}
    suspend fun getValidators() {}
    suspend fun mergeCoins() {}
    suspend fun moveCall() {}
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

    /**
     * A builder class for creating instances of [SuiRpcClient].
     */
    class Builder(private var endpoint: EndPoint) {

        private var httpClient = HttpClient(CIO)

        /**
         * Sets the [HttpClient] to use for making HTTP requests.
         */
        fun httpClient(httpClient: HttpClient) = apply { this.httpClient = httpClient }

        /**
         * Builds a new instance of [SuiRpcClient].
         */
        fun build() = SuiRpcClient(endpoint, httpClient)
    }
}

fun SuiRpcClient.createClient(suiRpcClient: SuiRpcClient, block: SuiRpcClient) : SuiRpcClient {
    return suiRpcClient
}
