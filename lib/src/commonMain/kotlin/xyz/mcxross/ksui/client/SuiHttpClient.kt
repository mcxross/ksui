package xyz.mcxross.ksui.client

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.network.*
import io.ktor.utils.io.errors.*
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.serializer
import xyz.mcxross.ksui.exception.SuiException
import xyz.mcxross.ksui.exception.TransactionNotFoundException
import xyz.mcxross.ksui.exception.UnresolvedSuiEndPointException
import xyz.mcxross.ksui.model.Balance
import xyz.mcxross.ksui.model.Checkpoint
import xyz.mcxross.ksui.model.CheckpointId
import xyz.mcxross.ksui.model.CheckpointPage
import xyz.mcxross.ksui.model.CheckpointSequenceNumber
import xyz.mcxross.ksui.model.CoinPage
import xyz.mcxross.ksui.model.CommitteeInfo
import xyz.mcxross.ksui.model.DelegatedStake
import xyz.mcxross.ksui.model.Discovery
import xyz.mcxross.ksui.model.Event
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.EventID
import xyz.mcxross.ksui.model.EventPage
import xyz.mcxross.ksui.model.GasPrice
import xyz.mcxross.ksui.model.LoadedChildObjectsResponse
import xyz.mcxross.ksui.model.MoveFunctionArgType
import xyz.mcxross.ksui.model.MoveNormalizedFunction
import xyz.mcxross.ksui.model.MoveNormalizedModule
import xyz.mcxross.ksui.model.ObjectResponse
import xyz.mcxross.ksui.model.ObjectResponseQuery
import xyz.mcxross.ksui.model.ObjectsPage
import xyz.mcxross.ksui.model.Response
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiCoinMetadata
import xyz.mcxross.ksui.model.SuiSystemStateSummary
import xyz.mcxross.ksui.model.Supply
import xyz.mcxross.ksui.model.TransactionBlockResponse
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionBlockResponseQuery
import xyz.mcxross.ksui.model.TransactionBlocksPage
import xyz.mcxross.ksui.model.TransactionDigest
import xyz.mcxross.ksui.model.ValidatorApys

/** A Kotlin wrapper around the Sui JSON-RPC API for interacting with a Sui full node. */
class SuiHttpClient(override val configContainer: ConfigContainer) : SuiClient {

  internal val json = Json {
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
  @OptIn(ExperimentalSerializationApi::class)
  @Throws(
      SuiException::class,
      CancellationException::class,
      IOException::class,
      IllegalStateException::class,
  )
  private suspend fun call(method: String, vararg params: Any?): HttpResponse {
    val response: HttpResponse
    try {
      response =
          configContainer.httpClient.post {
            url(whichUrl(configContainer.endPoint))
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                      put("jsonrpc", "2.0")
                      put("id", 1)
                      put("method", method)
                      // add an array of params to the request body
                      putJsonArray("params") {
                        params.forEach {
                          when (it) {
                            is String -> add(it)
                            is Int -> add(it)
                            is Long -> add(it)
                            is Boolean -> add(it)
                            null -> add(it)
                            is JsonElement -> add(it)
                            is List<*> -> addJsonArray { it.forEach { i -> add(i.toString()) } }
                            else -> add(json.encodeToString(serializer(), it))
                          }
                        }
                      }
                    }
                    .toString())
          }
    } catch (e: UnresolvedAddressException) {
      throw UnresolvedSuiEndPointException(
          "Couldn't resolve endpoint: ${whichUrl(configContainer.endPoint)}")
    }

    if (response.status.isSuccess()) {
      return response
    } else {
      throw SuiException(response.status.value.toString())
    }
  }

  /**
   * Pings Sui using the configured endpoint URL and HTTP client.
   *
   * @return `true` if the Sui is available and returns a successful response, `false` otherwise.
   */
  suspend fun isSuiAvailable(): Boolean = call("isSuiAvailable").status.isSuccess()

  /**
   * Suspended function that retrieves the [Discovery] data from the JSON-RPC API service using the
   * `rpc.discover` method. This method provides documentation describing the available JSON-RPC
   * APIs.
   *
   * @return [Discovery] object containing the discovered APIs.
   * @throws [SuiException] if an error occurs while decoding the JSON response or if the response
   *   is an error.
   */
  suspend fun discover(): Discovery {
    when (val result =
        json.decodeFromString<Response<Discovery>>(
            serializer(), call("rpc.discover").bodyAsText())) {
      is Response.Ok -> return result.data
      is Response.Error -> throw SuiException(result.message)
    }
  }

  /**
   * Suspended generic function that calls a custom JSON-RPC API method with the given name and
   * parameters.
   *
   * This function is useful for calling JSON-RPC APIs that are not yet supported by this library.
   *
   * @param function The name of the JSON-RPC API method to call.
   * @param params The parameters to pass to the JSON-RPC API method.
   * @return a [T] object containing the result of the JSON-RPC API method.
   * @throws [SuiException] if an error occurs while decoding the JSON response or if the response
   *   is an error.
   */
  suspend fun <T> getCustom(function: String, vararg params: Any?): T {
    val result =
        json.decodeFromString<Response<T>>(serializer(), call(function, *params).bodyAsText())
    when (result) {
      is Response.Ok -> return result.data
      is Response.Error -> throw SuiException(result.message)
    }
  }

  /**
   * Return the total coin balance for all coin type, owned by the address owner.
   *
   * @param owner's Sui address.
   * @return [List<[Balance]>]
   */
  suspend fun getAllBalances(owner: SuiAddress): List<Balance> {
    val result =
        json.decodeFromString<Response<List<Balance>>>(
            serializer(), call("suix_getAllBalances", owner.pubKey).bodyAsText())
    when (result) {
      is Response.Ok -> return result.data
      is Response.Error -> throw SuiException(result.message)
    }
  }

  /**
   * Return the total coin balance for one coin type, owned by the address owner.
   *
   * @param owner's Sui address.
   * @param coinType names for the coin. Defaults to "0x2::sui::SUI"
   * @return [Balance]
   */
  suspend fun getBalance(owner: SuiAddress, coinType: String = "0x2::sui::SUI"): Balance {
    val response =
        json.decodeFromString<Response<Balance>>(
            serializer(),
            call("suix_getBalance", *listOf(owner.pubKey, coinType).toTypedArray()).bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return all Coin objects owned by an address.
   *
   * @param owner's Sui address.
   * @param cursor pagination cursor. This is optional.
   * @param limit pagination limit
   * @return [Balance]
   */
  suspend fun getAllCoins(
      owner: SuiAddress,
      cursor: String? = null,
      limit: Long,
  ): CoinPage {
    val response =
        json.decodeFromString<Response<CoinPage>>(
            serializer(),
            call("suix_getAllCoins", *listOf(owner.pubKey, cursor, limit).toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Suspended function that retrieves the chain's identifier.
   *
   * @return The chain identifier as a string.
   * @throws SuiException if there is an error retrieving the chain identifier.
   */
  suspend fun getChainIdentifier(): String {
    when (val response =
        json.decodeFromString<Response<String>>(
            serializer(), call("sui_getChainIdentifier").bodyAsText())) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return a checkpoint
   *
   * @param checkpointId Checkpoint identifier, can use either checkpoint digest, or checkpoint
   *   sequence number as input.
   * @return [Checkpoint]
   * @throws [SuiException] if there is an error fetching the checkpoint *
   */
  suspend fun getCheckpoint(checkpointId: CheckpointId): Checkpoint {
    val response =
        json.decodeFromString<Response<Checkpoint>>(
            serializer(), call("sui_getCheckpoint", checkpointId.digest).bodyAsText())

    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Fetches paginated list of checkpoints.
   *
   * @param cursor the cursor to start fetching from
   * @param limit the maximum number of checkpoints to fetch
   * @param descendingOrder whether to return the checkpoints in descending order (defaults to
   *   false)
   * @return a [CheckpointPage] object representing the fetched checkpoints
   * @throws [SuiException] if there is an error fetching the checkpoints
   */
  suspend fun getCheckpoints(
      cursor: Int? = null,
      limit: Long? = null,
      descendingOrder: Boolean = false
  ): CheckpointPage {
    val response =
        json.decodeFromString<Response<CheckpointPage>>(
            serializer(),
            call(
                    "sui_getCheckpoints",
                    *listOf(cursor?.toString(), limit, descendingOrder).toTypedArray())
                .bodyAsText())

    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves a list of events associated with a given transaction digest.
   *
   * @param digest the transaction digest to retrieve events for
   * @return a list of events associated with the given transaction digest
   * @throws SuiException if an error occurs while retrieving the events
   */
  suspend fun getEvents(digest: TransactionDigest): List<Event> {
    val response =
        json.decodeFromString<Response<List<Event>>>(
            serializer(), call("sui_getEvents", digest.value).bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return metadata(e.g., symbol, decimals) for a coin
   *
   * @param coinType name for the coin.
   * @return [SuiCoinMetadata]
   */
  suspend fun getCoinMetadata(coinType: String): SuiCoinMetadata {
    val response =
        json.decodeFromString<Response<SuiCoinMetadata>>(
            serializer(),
            call("suix_getCoinMetadata", *listOf(coinType).toTypedArray()).bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves information about coins held by a given SUI address, with optional filtering by coin
   * type and paging parameters.
   *
   * @param address The SUI address for which to retrieve coin information.
   * @param coinType The type of coins to retrieve. This is optional, and if null, defaults to
   *   `0x2::sui::SUI`
   * @param cursor The index of the first item to retrieve. If null, the first item will be
   *   retrieved.
   * @param limit The maximum number of items to retrieve.
   * @return A [CoinPage] object containing information about the coins.
   * @throws SuiException if an error occurs while retrieving the coin information.
   */
  suspend fun getCoins(
      owner: SuiAddress,
      coinType: String? = null,
      cursor: Int? = null,
      limit: Int
  ): CoinPage {
    val response =
        json.decodeFromString<Response<CoinPage>>(
            serializer(),
            call("suix_getCoins", *listOf(owner.pubKey, coinType, cursor, limit).toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Suspended function to retrieve information about the committee information for the asked
   * `epoch`.
   *
   * @param epoch the epoch to retrieve committee information for, defaults to null (SUi will use
   *   the current epoch if not specified)
   * @return an instance of [CommitteeInfo.SuiCommittee]
   * @throws SuiException if an error occurred while retrieving committee information
   */
  suspend fun getCommitteeInfo(epoch: Long? = null): CommitteeInfo.SuiCommittee {
    // Decode response using JSON deserialization
    val response =
        json.decodeFromString<Response<CommitteeInfo.SuiCommittee>>(
            serializer(),
            when (epoch) {
              null -> call("suix_getCommitteeInfo").bodyAsText()
              else -> call("suix_getCommitteeInfo", epoch).bodyAsText()
            })
    // Return data or throw an exception if response is an error
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return the latest SUI system state object on-chain.
   *
   * @return [SuiSystemStateSummary]
   */
  suspend fun getLatestSuiSystemState(): SuiSystemStateSummary {
    val response =
        json.decodeFromString<Response<SuiSystemStateSummary>>(
            serializer(), call("suix_getLatestSuiSystemState").bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves the owned objects based on the provided address, query, cursor, and limit.
   *
   * @param address The SuiAddress representing the owner's address.
   * @param query The [ObjectResponseQuery] to filter the owned objects.
   * @param cursor The cursor indicating the starting point of the query results (optional).
   * @param limit The maximum number of objects to retrieve.
   * @return The [ObjectsPage] containing the owned objects.
   * @throws SuiException if an error occurs while retrieving the owned objects.
   */
  suspend fun getOwnedObjects(
      address: SuiAddress,
      query: ObjectResponseQuery,
      cursor: String? = null,
      limit: Int
  ): ObjectsPage {
    val response =
        json.decodeFromString<Response<ObjectsPage>>(
            serializer(),
            call(
                    "suix_getOwnedObjects",
                    address.pubKey,
                    json.encodeToJsonElement(serializer(), query),
                    cursor,
                    limit)
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return the sequence number of the latest checkpoint that has been executed
   *
   * @return [CheckpointSequenceNumber]
   */
  suspend fun getLatestCheckpointSequenceNumber(): CheckpointSequenceNumber {
    val response =
        json.decodeFromString<Response<Long>>(
            serializer(), call("sui_getLatestCheckpointSequenceNumber").bodyAsText())
    when (response) {
      is Response.Ok -> return CheckpointSequenceNumber(response.data)
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves the loaded child objects for a given transaction digest.
   *
   * @param digest The transaction digest for which to retrieve the loaded child objects.
   * @return [LoadedChildObjectsResponse] containing the loaded child objects.
   * @throws SuiException if an error occurs while retrieving the loaded child objects.
   */
  suspend fun getLoadedChildObjects(digest: TransactionDigest): LoadedChildObjectsResponse {
    val response =
        json.decodeFromString<Response<LoadedChildObjectsResponse>>(
            serializer(), call("sui_getLoadedChildObjects", digest.value).bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves the argument types for a Move function from the blockchain.
   *
   * @param pakage the package name of the module containing the function
   * @param module the module name containing the function
   * @param function the name of the function to retrieve the argument types for
   * @return a list of [MoveFunctionArgType] objects representing the argument types for the
   *   function
   * @throws SuiException if there is an error retrieving the argument types from the blockchain
   */
  suspend fun getMoveFunctionArgTypes(
      pakage: String,
      module: String,
      function: String
  ): List<MoveFunctionArgType> {
    val response =
        json.decodeFromString<Response<List<MoveFunctionArgType>>>(
            serializer(),
            call("sui_getMoveFunctionArgTypes", *listOf(pakage, module, function).toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves a normalized representation of a Move function from the blockchain.
   *
   * @param pakage the package name of the module containing the function
   * @param module the module name containing the function
   * @param function the name of the function to retrieve
   * @return a [MoveNormalizedFunction] object representing the normalized function
   * @throws SuiException if there is an error retrieving the function from the blockchain
   */
  suspend fun getNormalizedMoveFunction(
      pakage: String,
      module: String,
      function: String
  ): MoveNormalizedFunction {
    val response =
        json.decodeFromString<Response<MoveNormalizedFunction>>(
            serializer(),
            call("sui_getNormalizedMoveFunction", *listOf(pakage, module, function).toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves a normalized representation of a Move module from the blockchain.
   *
   * @param pakage the package name of the module to retrieve
   * @param module the name of the module to retrieve
   * @return a [MoveNormalizedModule] object representing the normalized module
   * @throws SuiException if there is an error retrieving the module from the blockchain
   */
  suspend fun getNormalizedMoveModule(pakage: String, module: String): MoveNormalizedModule {
    val response =
        json.decodeFromString<Response<MoveNormalizedModule>>(
            serializer(),
            call("sui_getNormalizedMoveModule", *listOf(pakage, module).toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves structured representations of all modules in the given package.
   *
   * @param pakage The package name to retrieve move modules from.
   * @return A map of module names to MoveNormalizedModule objects.
   * @throws SuiException if there is an error in the response.
   */
  suspend fun getNormalizedMoveModulesByPackage(pakage: String): Map<String, MoveNormalizedModule> {
    val response =
        json.decodeFromString<Response<Map<String, MoveNormalizedModule>>>(
            serializer(),
            call("sui_getNormalizedMoveModulesByPackage", *listOf(pakage).toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Asynchronously retrieves an object with the given [objectId] using the SUI API, with the
   * specified [options].
   *
   * @param objectId the ID of the object to retrieve.
   * @param options additional options to control the object data returned.
   * @return a suspended [ObjectResponse] containing the retrieved object data.
   * @throws SuiException if the API request fails or returns an error response.
   */
  suspend fun getObject(
      objectId: String,
      options: ObjectResponse.ObjectDataOptions
  ): ObjectResponse {
    val response =
        json.decodeFromString<Response<ObjectResponse>>(
            serializer(),
            call(
                    "sui_getObject",
                    *listOf(objectId, json.encodeToJsonElement(serializer(), options))
                        .toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return the reference gas price for the network.
   *
   * @return [GasPrice]
   */
  suspend fun getReferenceGasPrice(): GasPrice {
    val response =
        json.decodeFromString<Response<Long>>(
            serializer(), call("suix_getReferenceGasPrice").bodyAsText())
    when (response) {
      is Response.Ok -> return GasPrice(response.data)
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves the list of delegated stakes for a specified owner.
   *
   * @param owner The owner of the stakes to retrieve.
   * @return The list of delegated stakes for the specified owner.
   * @throws SuiException if there is an error retrieving the stakes.
   */
  suspend fun getStakes(owner: SuiAddress): List<DelegatedStake> {
    val response =
        json.decodeFromString<Response<List<DelegatedStake>>>(
            serializer(), call("suix_getStakes", owner.pubKey).bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves the total supply for a specified cryptocurrency.
   *
   * @param coinType The type of cryptocurrency to retrieve the supply for.
   * @return The total supply of the specified cryptocurrency.
   * @throws SuiException if there is an error retrieving the supply.
   */
  suspend fun getTotalSupply(coinType: String): Supply {
    val response =
        json.decodeFromString<Response<Supply>>(
            serializer(), call("suix_getTotalSupply", coinType).bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Suspended function that retrieves the APYs (Annual Percentage Yields) for validators.
   *
   * @return An instance of ValidatorApys containing the APY data for validators.
   * @throws SuiException if there is an error retrieving the APY data.
   */
  suspend fun getValidatorApys(): ValidatorApys {
    when (val response =
        json.decodeFromString<Response<ValidatorApys>>(
            serializer(), call("suix_getValidatorsApy").bodyAsText())) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Queries events on the Sui blockchain matching the given [eventFilter].
   *
   * It optionally starts from the event with the specified [cursor], returning at most [limit]
   * events, otherwise `QUERY_MAX_RESULT_LIMIT`, and in either ascending or descending order based
   * on [descendingOrder]. Returns an [EventPage] containing the matching events and pagination
   * information.
   *
   * @param eventFilter the query criteria used to match events on the blockchain
   * @param cursor the event ID from which to start querying events, defaults to null which will
   *   start from the beginning
   * @param limit the maximum number of events to return, default to [QUERY_MAX_RESULT_LIMIT] if not
   *   specified.
   * @param descendingOrder the order in which to return the events, default to false (ascending
   *   order), oldest record first.
   * @return an [EventPage] containing the matching events and pagination information
   * @throws SuiException if there is an error querying the events
   */
  suspend fun queryEvents(
      eventFilter: EventFilter,
      cursor: EventID? = null,
      limit: Int? = null,
      descendingOrder: Boolean? = null
  ): EventPage {
    val response =
        json.decodeFromString<Response<EventPage>>(
            serializer(),
            call(
                    "suix_queryEvents",
                    *listOf(
                            json.encodeToJsonElement(EventFilter.serializer(), eventFilter),
                            cursor?.txDigest,
                            limit,
                            descendingOrder)
                        .toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Return the total number of transactions known to the server.
   *
   * @return [Long]
   */
  suspend fun getTotalTransactionBlocks(): Long {
    val response =
        json.decodeFromString<Response<Long>>(
            serializer(), call("sui_getTotalTransactionBlocks").bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Retrieves the transaction block corresponding to the given digest and with the specified
   * options.
   *
   * @param digest the [TransactionDigest] object representing the digest of the transaction block
   *   to retrieve.
   * @param options an optional [TransactionBlockResponseOptions] object specifying additional
   *   options for the response. Defaults to null.
   * @return a [TransactionBlockResponse] object containing the transaction block data.
   * @throws TransactionNotFoundException if the transaction block is not found.
   * @throws SuiException if there is an error retrieving the transaction block.
   */
  suspend fun getTransactionBlock(
      digest: TransactionDigest,
      options: TransactionBlockResponseOptions? = null
  ): TransactionBlockResponse {

    val response =
        json.decodeFromString<Response<TransactionBlockResponse>>(
            serializer(),
            call(
                    "sui_getTransactionBlock",
                    *listOf(digest.value, json.encodeToJsonElement(serializer(), options))
                        .toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error ->
          if (response.message.contains("not find")) {
            throw TransactionNotFoundException(digest.value)
          } else {
            throw SuiException(response.message)
          }
    }
  }

  /**
   * Retrieves multiple transaction blocks corresponding to the given digests and with the specified
   * options.
   *
   * @param digests a list of [TransactionDigest] objects representing the digests of the
   *   transaction blocks to retrieve.
   * @param options an optional [TransactionBlockResponseOptions] object specifying additional
   *   options for the response. Defaults to null.
   * @return a list of [TransactionBlockResponse] objects containing the transaction block data.
   * @throws SuiException if the response from the server is an error.
   */
  suspend fun getMultiTransactionBlocks(
      digests: List<TransactionDigest>,
      options: TransactionBlockResponseOptions? = null
  ): List<TransactionBlockResponse> {
    val response =
        json.decodeFromString<Response<List<TransactionBlockResponse>>>(
            serializer(),
            call(
                    "sui_multiGetTransactionBlocks",
                    *listOf(
                            digests.map { it.value },
                            json.encodeToJsonElement(serializer(), options))
                        .toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }

  /**
   * Queries the transaction blocks based on the given parameters.
   *
   * @param query the [TransactionBlockResponseQuery] object containing the query parameters.
   * @param cursor an optional string representing the cursor to use for pagination. Defaults to
   *   null.
   * @param limit an integer representing the maximum number of results to return. Defaults to
   *   QUERY_MAX_RESULT_LIMIT if null.
   * @param descendingOrder a boolean indicating whether the results should be sorted in descending
   *   order. Defaults to false (ascending order), oldest record first.
   * @return a [TransactionBlocksPage] object containing the transaction blocks that match the query
   *   parameters.
   * @throws SuiException if the response from the server is an error.
   */
  suspend fun queryTransactionBlocks(
      query: TransactionBlockResponseQuery,
      cursor: String? = null,
      limit: Int? = null,
      descendingOrder: Boolean = false
  ): TransactionBlocksPage {
    val response =
        json.decodeFromString<Response<TransactionBlocksPage>>(
            serializer(),
            call(
                    "suix_queryTransactionBlocks",
                    *listOf(
                            json.encodeToJsonElement(serializer(), query),
                            cursor,
                            limit,
                            descendingOrder)
                        .toTypedArray())
                .bodyAsText())
    when (response) {
      is Response.Ok -> return response.data
      is Response.Error -> throw SuiException(response.message)
    }
  }
}
