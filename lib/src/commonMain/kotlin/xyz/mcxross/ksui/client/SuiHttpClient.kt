package xyz.mcxross.ksui.client

import io.ktor.client.call.*
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
import xyz.mcxross.ksui.model.DryRunTransactionBlockResponse
import xyz.mcxross.ksui.model.DynamicFieldName
import xyz.mcxross.ksui.model.Event
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.EventID
import xyz.mcxross.ksui.model.EventPage
import xyz.mcxross.ksui.model.ExecuteTransactionRequestType
import xyz.mcxross.ksui.model.GasPrice
import xyz.mcxross.ksui.model.LoadedChildObjectsResponse
import xyz.mcxross.ksui.model.MoveFunctionArgType
import xyz.mcxross.ksui.model.MoveNormalizedFunction
import xyz.mcxross.ksui.model.MoveNormalizedModule
import xyz.mcxross.ksui.model.NameServicePage
import xyz.mcxross.ksui.model.ObjectId
import xyz.mcxross.ksui.model.ObjectResponse
import xyz.mcxross.ksui.model.ObjectResponseQuery
import xyz.mcxross.ksui.model.ObjectsPage
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.PastObjectRequest
import xyz.mcxross.ksui.model.PastObjectResponse
import xyz.mcxross.ksui.model.Response
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.SuiCoinMetadata
import xyz.mcxross.ksui.model.SuiSystemStateSummary
import xyz.mcxross.ksui.model.Supply
import xyz.mcxross.ksui.model.TransactionBlockBuilderMode
import xyz.mcxross.ksui.model.TransactionBlockBytes
import xyz.mcxross.ksui.model.TransactionBlockResponse
import xyz.mcxross.ksui.model.TransactionBlockResponseOptions
import xyz.mcxross.ksui.model.TransactionBlockResponseQuery
import xyz.mcxross.ksui.model.TransactionBlocksPage
import xyz.mcxross.ksui.model.TransactionDigest
import xyz.mcxross.ksui.model.TypeTag
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
  private suspend inline fun <reified T> call(method: String, vararg params: Any?): T {
    val resp: HttpResponse
    try {
      resp =
        configContainer.httpClient().post {
          url(whichUrl(configContainer.endPoint))
          contentType(ContentType.Application.Json)
          setBody(
            buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", method)
                // add an array of params to the request body
                putJsonArray("params") {
                  params.forEach { it ->
                    when (it) {
                      is String -> add(it)
                      is Int -> add(it)
                      is Long -> add(it)
                      is Boolean -> add(it)
                      null -> add(it)
                      is JsonElement -> add(it)
                      is List<*> ->
                        addJsonArray {
                          it.forEach { item ->
                            when (item) {
                              is String -> add(item)
                              is Int -> add(item)
                              is JsonElement -> add(item)
                            }
                          }
                        }
                      else -> add(json.encodeToString(serializer(), it))
                    }
                  }
                }
              }
              .toString()
          )
        }
    } catch (e: UnresolvedAddressException) {
      throw UnresolvedSuiEndPointException(
        "Couldn't resolve endpoint: ${whichUrl(configContainer.endPoint)}"
      )
    }

    if (resp.status.isSuccess()) {
      return resp.body()
    } else {
      throw SuiException(resp.status.value.toString())
    }
  }

  /**
   * Pings Sui using the configured endpoint URL and HTTP client.
   *
   * @return `true` if the Sui is available and returns a successful response, `false` otherwise.
   */
  suspend fun isSuiAvailable(): Boolean = call<Boolean>("isSuiAvailable")

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
    when (val result = call<Response<Discovery>>("rpc.discover")) {
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
    when (val result = call<Response<T>>(function, *params)) {
      is Response.Ok -> return result.data
      is Response.Error -> throw SuiException(result.message)
    }
  }

  /**
   * Return transaction execution effects including the gas cost summary, while the effects are not
   * committed to the chain.
   *
   * @param txBytes The serialized transaction block bytes.
   * @return [DryRunTransactionBlockResponse] The response containing the result of the dry run.
   * @throws SuiException if there is an error during the dry run.
   */
  suspend fun dryRunTransactionBlock(txBytes: String): DryRunTransactionBlockResponse =
    when (
      val resp =
        call<Response<DryRunTransactionBlockResponse>>(
          "sui_dryRunTransactionBlock",
          *listOf(txBytes).toTypedArray(),
        )
    ) {
      is Response.Ok -> resp.data
      is Response.Error -> throw SuiException(resp.message)
    }

  /**
   * Execute the transaction and wait for results if desired.
   *
   * Request types: 1. WaitForEffectsCert: waits for TransactionEffectsCert and then return to
   * client. This mode is a proxy for transaction finality. 2. WaitForLocalExecution: waits for
   * TransactionEffectsCert and make sure the node executed the transaction locally before returning
   * the client. The local execution makes sure this node is aware of this transaction when client
   * fires subsequent queries. However, if the node fails to execute the transaction locally in a
   * timely manner, a bool type in the response is set to false indicating the case. request_type is
   * default to be `WaitForEffectsCert` unless options.show_events or options.show_effects is true
   *
   * @param txBytes BCS serialized transaction data bytes without its type tag, as base-64 encoded
   *   string.
   * @param signatures A list of signatures (`flag || signature || pubkey` bytes, as base-64 encoded
   *   string). Signature is committed to the intent message of the transaction data, as base-64
   *   encoded string.
   * @param options for specifying the content to be returned.
   * @param requestType The request type, derived from `[TransactionBlockResponseOptions]` if None.
   * @return [TransactionBlockResponse] The response from the transaction block execution.
   * @throws SuiException if the response is an error.
   */
  suspend fun executeTransactionBlock(
    txBytes: String,
    signatures: List<String>,
    options: TransactionBlockResponseOptions,
    requestType: ExecuteTransactionRequestType? = null,
  ): TransactionBlockResponse {
    val resp =
      call<Response<TransactionBlockResponse>>(
        "sui_executeTransactionBlock",
        *listOf(
            txBytes,
            signatures,
            json.encodeToJsonElement(serializer(), options),
            requestType?.value(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return the total coin balance for all coin type, owned by the address owner.
   *
   * @param owner's Sui address.
   * @return [List<[Balance]>]
   */
  suspend fun getAllBalances(owner: SuiAddress): List<Balance> {
    when (val result = call<Response<List<Balance>>>("suix_getAllBalances", owner.pubKey)) {
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
    val resp =
      call<Response<Balance>>("suix_getBalance", *listOf(owner.pubKey, coinType).toTypedArray())
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
  suspend fun getAllCoins(owner: SuiAddress, cursor: String? = null, limit: Long): CoinPage {
    when (
      val resp =
        call<Response<CoinPage>>(
          "suix_getAllCoins",
          *listOf(owner.pubKey, cursor, limit).toTypedArray(),
        )
    ) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Suspended function that retrieves the chain's identifier.
   *
   * @return The chain identifier as a string.
   * @throws SuiException if there is an error retrieving the chain identifier.
   */
  suspend fun getChainIdentifier(): String {
    when (val resp = call<Response<String>>("sui_getChainIdentifier")) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    when (val resp = call<Response<Checkpoint>>("sui_getCheckpoint", checkpointId.digest)) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    descendingOrder: Boolean = false,
  ): CheckpointPage {
    when (
      val resp =
        call<Response<CheckpointPage>>(
          "sui_getCheckpoints",
          *listOf(cursor?.toString(), limit, descendingOrder).toTypedArray(),
        )
    ) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    when (val resp = call<Response<List<Event>>>("sui_getEvents", digest.value)) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return metadata(e.g., symbol, decimals) for a coin
   *
   * @param coinType name for the coin.
   * @return [SuiCoinMetadata]
   */
  suspend fun getCoinMetadata(coinType: String): SuiCoinMetadata {
    when (
      val resp =
        call<Response<SuiCoinMetadata>>("suix_getCoinMetadata", *listOf(coinType).toTypedArray())
    ) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    limit: Int,
  ): CoinPage {
    when (
      val resp =
        call<Response<CoinPage>>(
          "suix_getCoins",
          *listOf(owner.pubKey, coinType, cursor, limit).toTypedArray(),
        )
    ) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    // Decode resp using JSON deserialization
    val resp =
      when (epoch) {
        null -> call<Response<CommitteeInfo.SuiCommittee>>("suix_getCommitteeInfo")
        else -> call<Response<CommitteeInfo.SuiCommittee>>("suix_getCommitteeInfo", epoch)
      }
    // Return data or throw an exception if resp is an error
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Retrieves the dynamic field object information for a specified object.
   *
   * @param parentObjectId The ID of the queried parent object.
   * @param name The Name of the dynamic field.
   * @return The response containing the dynamic field object.
   * @throws SuiException if there is an error in the response.
   */
  suspend fun getDynamicFieldObject(
    parentObjectId: ObjectId,
    name: DynamicFieldName,
  ): ObjectResponse {
    when (
      val resp =
        call<Response<ObjectResponse>>(
          "suix_getDynamicFieldObject",
          *listOf(parentObjectId.hash, json.encodeToJsonElement(serializer(), name)).toTypedArray(),
        )
    ) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return the latest SUI system state object on-chain.
   *
   * @return [SuiSystemStateSummary]
   */
  suspend fun getLatestSuiSystemState(): SuiSystemStateSummary {
    when (val resp = call<Response<SuiSystemStateSummary>>("suix_getLatestSuiSystemState")) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    limit: Int,
  ): ObjectsPage {
    val resp =
      call<Response<ObjectsPage>>(
        "suix_getOwnedObjects",
        address.pubKey,
        json.encodeToJsonElement(serializer(), query),
        cursor,
        limit,
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return the sequence number of the latest checkpoint that has been executed
   *
   * @return [CheckpointSequenceNumber]
   */
  suspend fun getLatestCheckpointSequenceNumber(): CheckpointSequenceNumber {
    when (val resp = call<Response<Long>>("sui_getLatestCheckpointSequenceNumber")) {
      is Response.Ok -> return CheckpointSequenceNumber(resp.data)
      is Response.Error -> throw SuiException(resp.message)
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
    val resp = call<Response<LoadedChildObjectsResponse>>("sui_getLoadedChildObjects", digest.value)
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    function: String,
  ): List<MoveFunctionArgType> {
    val resp =
      call<Response<List<MoveFunctionArgType>>>(
        "sui_getMoveFunctionArgTypes",
        *listOf(pakage, module, function).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    function: String,
  ): MoveNormalizedFunction {
    val resp =
      call<Response<MoveNormalizedFunction>>(
        "sui_getNormalizedMoveFunction",
        *listOf(pakage, module, function).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    val resp =
      call<Response<MoveNormalizedModule>>(
        "sui_getNormalizedMoveModule",
        *listOf(pakage, module).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    val resp =
      call<Response<Map<String, MoveNormalizedModule>>>(
        "sui_getNormalizedMoveModulesByPackage",
        *listOf(pakage).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    options: ObjectResponse.ObjectDataOptions,
  ): ObjectResponse {
    val resp =
      call<Response<ObjectResponse>>(
        "sui_getObject",
        *listOf(objectId, json.encodeToJsonElement(serializer(), options)).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return the reference gas price for the network.
   *
   * @return [GasPrice]
   */
  suspend fun getReferenceGasPrice(): GasPrice {
    when (val resp = call<Response<Long>>("suix_getReferenceGasPrice")) {
      is Response.Ok -> return GasPrice(resp.data)
      is Response.Error -> throw SuiException(resp.message)
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
    when (val resp = call<Response<List<DelegatedStake>>>("suix_getStakes", owner.pubKey)) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Retrieves one or more [DelegatedStake].
   *
   * If a Stake was withdrawn its status will be Unstaked. Fails if either one of the stakedSuiId is
   * not found.
   *
   * @param stakedSuiIds The list of stake IDs to retrieve.
   * @return The list of delegated stakes matching the provided IDs.
   * @throws SuiException if an error occurs during the retrieval process.
   */
  suspend fun getStakesByIds(stakedSuiIds: List<ObjectId>): List<DelegatedStake> {
    val resp =
      call<Response<List<DelegatedStake>>>("suix_getStakesByIds", stakedSuiIds.map { it.hash })
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    when (val resp = call<Response<Supply>>("suix_getTotalSupply", coinType)) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Suspended function that retrieves the APYs (Annual Percentage Yields) for validators.
   *
   * @return An instance of ValidatorApys containing the APY data for validators.
   * @throws SuiException if there is an error retrieving the APY data.
   */
  suspend fun getValidatorApys(): ValidatorApys {
    when (val resp = call<Response<ValidatorApys>>("suix_getValidatorsApy")) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Queries events on the Sui blockchain matching the given [eventFilter]. d It optionally starts
   * from the event with the specified [cursor], returning at most [limit] events, otherwise
   * `QUERY_MAX_RESULT_LIMIT`, and in either ascending or descending order based on
   * [descendingOrder]. Returns an [EventPage] containing the matching events and pagination
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
    descendingOrder: Boolean? = null,
  ): EventPage {
    val resp =
      call<Response<EventPage>>(
        "suix_queryEvents",
        *listOf(
            json.encodeToJsonElement(EventFilter.serializer(), eventFilter),
            cursor?.txDigest,
            limit,
            descendingOrder,
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return the total number of transactions known to the server.
   *
   * @return [Long]
   */
  suspend fun getTotalTransactionBlocks(): Long {
    when (val resp = call<Response<Long>>("sui_getTotalTransactionBlocks")) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    options: TransactionBlockResponseOptions? = null,
  ): TransactionBlockResponse {
    val resp =
      call<Response<TransactionBlockResponse>>(
        "sui_getTransactionBlock",
        *listOf(digest.value, json.encodeToJsonElement(serializer(), options)).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error ->
        if (resp.message.contains("not find")) {
          throw TransactionNotFoundException(digest.value)
        } else {
          throw SuiException(resp.message)
        }
    }
  }

  /**
   * Retrieves the object data for a list of objects.
   *
   * @param objectIds the IDs of the queried objects.
   * @param options options for specifying the content to be returned.
   * @return A list of ObjectResponse objects.
   * @throws SuiException if an error occurs during the retrieval process.
   */
  suspend fun getMultiObjects(
    objectIds: List<ObjectId>,
    options: ObjectResponse.ObjectDataOptions,
  ): List<ObjectResponse> {
    val resp =
      call<Response<List<ObjectResponse>>>(
        "sui_multiGetObjects",
        *listOf(objectIds.map { it.hash }, json.encodeToJsonElement(serializer(), options))
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    options: TransactionBlockResponseOptions? = null,
  ): List<TransactionBlockResponse> {
    val resp =
      call<Response<List<TransactionBlockResponse>>>(
        "sui_multiGetTransactionBlocks",
        *listOf(digests.map { it.value }, json.encodeToJsonElement(serializer(), options))
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Tries to retrieve a past version of an object based on its ID and version number.
   *
   * Note: There is no software-level guarantee/SLA that objects with past versions can be retrieved
   * by this API, even if the object and version exists/existed. The result may vary across nodes
   * depending on their pruning policies. Return the object information for a specified version
   *
   * @param objectId the ID of the queried object.
   * @param version the version of the queried object. If None, default to the latest known version.
   * @param options for specifying the content to be returned.
   * @return An Option containing a PastObjectResponse if the retrieval is successful, or None if
   *   the object/version is not found.
   * @throws SuiException if an error occurs during the retrieval process.
   */
  suspend fun tryGetPastObject(
    objectId: ObjectId,
    version: Long,
    options: ObjectResponse.ObjectDataOptions,
  ): Option<PastObjectResponse> {
    val resp =
      call<Response<Option<PastObjectResponse>>>(
        "sui_tryGetPastObject",
        *listOf(objectId.hash, version, json.encodeToJsonElement(serializer(), options))
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Tries to retrieve multiple past versions of objects based on the provided PastObjectRequest
   * list and options.
   *
   * Note: There is no software-level guarantee/SLA that objects with past versions can be retrieved
   * by this API, even if the object and version exists/existed. The result may vary across nodes
   * depending on their pruning policies. Return the object information for a specified version
   *
   * @param pastObjects The list of PastObjectRequest objects containing the object IDs and version
   *   numbers.
   * @param options The options for retrieving object data.
   * @return A list of Option<PastObjectResponse> where each element represents a past object if
   *   found, or None if not found.
   * @throws SuiException if an error occurs during the retrieval process.
   */
  suspend fun tryGetMultiPastObjects(
    pastObjects: List<PastObjectRequest>,
    options: ObjectResponse.ObjectDataOptions,
  ): List<Option<PastObjectResponse>> {
    val resp =
      call<Response<List<Option<PastObjectResponse>>>>(
        "sui_tryMultiGetPastObjects",
        *listOf(
            pastObjects.map {
              json.encodeToJsonElement(
                serializer(),
                buildJsonObject {
                  put("objectId", it.objectId.hash)
                  put("version", it.version.toString())
                },
              )
            },
            json.encodeToJsonElement(serializer(), options),
          )
          .toTypedArray(),
      )

    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
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
    descendingOrder: Boolean = false,
  ): TransactionBlocksPage {
    val resp =
      call<Response<TransactionBlocksPage>>(
        "suix_queryTransactionBlocks",
        *listOf(json.encodeToJsonElement(serializer(), query), cursor, limit, descendingOrder)
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Resolves the address of a name service based on the provided name.
   *
   * @param name The name to resolve.
   * @return The resolved address.
   * @throws SuiException if there is an error in the response.
   */
  suspend fun resolveNameServiceAddress(name: String): String {
    when (val resp = call<Response<String>>("suix_resolveNameServiceAddress", name)) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Return the resolved names given address, if multiple names are resolved, the first one is the
   * primary name.
   *
   * @param address The list of addresses to resolve.
   * @param cursor The cursor to start fetching from. This is optional.
   * @param limit The maximum number of names to fetch.
   * @return A [NameServicePage] object containing the resolved names.
   * @throws SuiException if there is an error in the response.
   */
  suspend fun resolveNameServiceNames(
    address: SuiAddress,
    cursor: ObjectId? = null,
    limit: Long? = null,
  ): NameServicePage {
    when (
      val resp =
        call<Response<NameServicePage>>(
          "suix_resolveNameServiceNames",
          address.pubKey,
          cursor?.hash,
          limit,
        )
    ) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to merge multiple coins into one coin.
   *
   * @param signer The transaction signer's Sui address.
   * @param primaryCoin The coin object to merge into, this coin will remain after the transaction.
   * @param coinToMerge The coin object to be merged, this coin will be destroyed, the balance will
   *   be added to `[primaryCoin]`.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the coin merge is completed.
   * @throws SuiException if there is an error during the coin merge process.
   */
  suspend fun mergeCoins(
    signer: SuiAddress,
    primaryCoin: ObjectId,
    coinToMerge: ObjectId,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_mergeCoins",
        *listOf(signer.pubKey, primaryCoin.hash, coinToMerge.hash, gas?.hash, gasBudget.toString())
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to execute a Move call on the network, by calling the specified
   * function in the module of a given package.
   *
   * @param signer The transaction signer's Sui address
   * @param packageObjectId The Move package ID, e.g. `0x2`
   * @param module The Move module name, e.g. `pay`
   * @param function The move function name, e.g. `split`
   * @param typeArguments The type arguments of the Move function
   * @param arguments The arguments to be passed into the Move function, in SuiJson format
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget
   * @param executionMode Whether this is a Normal transaction or a Dev Inspect Transaction. Default
   *   to be `SuiTransactionBlockBuilderMode::Commit` when it's None
   * @return The resulting [TransactionBlockBytes]
   * @throws SuiException if there is an error in the response.
   */
  suspend fun moveCall(
    signer: SuiAddress,
    packageObjectId: ObjectId,
    module: String,
    function: String,
    typeArguments: List<TypeTag>,
    arguments: List<Any>,
    gas: ObjectId? = null,
    gasBudget: Long,
    executionMode: TransactionBlockBuilderMode? = null,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_moveCall",
        *listOf(
            signer.pubKey,
            packageObjectId.hash,
            module,
            function,
            typeArguments.map { it.asMoveType() },
            arguments.toTypedArray(),
            gas,
            gasBudget.toString(),
            executionMode,
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Send `Coin<T>` to a list of addresses, where `T` can be any coin type following a list of
   * amounts.
   *
   * The object specified in the `gas` field will be used to pay the gas fee for the transaction.
   * The gas object can not appear in `input_coins`. If the gas object is not specified, the RPC
   * server will auto-select one.
   *
   * @param signer The transaction signer's Sui address.
   * @param inputCoins The list of Sui coins to be used in this transaction.
   * @param recipients The list of recipients' addresses, the length of this vector must be the same
   *   as amounts.
   * @param amounts The amounts to be transferred to recipients, following the same order.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the payment is completed.
   * @throws SuiException if there is an error during the payment process.
   */
  suspend fun pay(
    signer: SuiAddress,
    inputCoins: List<ObjectId>,
    recipients: List<SuiAddress>,
    amounts: List<Long>,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_pay",
        *listOf(
            signer.pubKey,
            inputCoins.map { it.hash },
            recipients.map { it.pubKey },
            amounts.map { it.toString() },
            gas?.hash,
            gasBudget.toString(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Send all SUI coins to one recipient.
   *
   * This is for SUI coin only and does not require a separate gas coin object. Specifically, what
   * pay_all_sui does are: 1. accumulate all SUI from input coins and deposit all SUI to the first
   * input coin 2. transfer the updated first coin to the recipient and also use this first coin as
   * gas coin object. 3. the balance of the first input coin after tx is sum(input_coins) -
   * actual_gas_cost. 4. all other input coins other than the first are deleted.
   *
   * @param signer The transaction signer's Sui address.
   * @param inputCoins The Sui coins to be used in this transaction, including the coin for gas
   *   payment.
   * @param recipient The recipients' Sui address.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the publishing is completed.
   * @throws SuiException if there is an error during the publishing process.
   */
  suspend fun payAllSui(
    signer: SuiAddress,
    inputCoins: List<ObjectId>,
    recipient: SuiAddress,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_payAllSui",
        *listOf(signer.pubKey, inputCoins.map { it.hash }, recipient.pubKey, gasBudget.toString())
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Send SUI coins to a list of addresses, following a list of amounts.
   *
   * This is for SUI coin only and does not require a separate gas coin object. Specifically, what
   * pay_sui does are: 1. debit each input_coin to create new coin following the order of amounts
   * and assign it to the corresponding recipient. 2. accumulate all residual SUI from input coins
   * left and deposit all SUI to the first input coin, then use the first input coin as the gas coin
   * object. 3. the balance of the first input coin after tx is sum(input_coins) - sum(amounts) -
   * actual_gas_cost 4. all other input coints other than the first one are deleted.
   *
   * @param signer The transaction signer's Sui address.
   * @param inputCoins The Sui coins to be used in this transaction, including the coin for gas
   *   payment.
   * @param recipients The recipients' addresses, the length of this vector must be the same as
   *   amounts.
   * @param amounts The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the publishing is completed.
   * @throws SuiException if there is an error during the publishing process.
   */
  suspend fun paySui(
    signer: SuiAddress,
    inputCoins: List<ObjectId>,
    recipients: List<SuiAddress>,
    amounts: List<Long>,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_paySui",
        *listOf(
            signer.pubKey,
            inputCoins.map { it.hash },
            recipients.map { it.pubKey },
            amounts.map { it.toString() },
            gasBudget.toString(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to publish a Move package.
   *
   * @param sender The transaction signer's Sui address.
   * @param compiledModules The compiled bytes of a Move package.
   * @param dependencies The list of transitive dependency addresses that this set of modules
   *   depends on.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the publishing is completed.
   * @throws SuiException if there is an error during the publishing process.
   */
  suspend fun publish(
    sender: SuiAddress,
    compiledModules: List<String>,
    dependencies: List<ObjectId>,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_publish",
        *listOf(
            sender.pubKey,
            compiledModules,
            dependencies.map { it.hash },
            gas?.hash,
            gasBudget.toString(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Add stake to a validator's staking pool using multiple coins and amount.
   *
   * @param signer The transaction signer's Sui address.
   * @param coins The list of Coin Objects to stake.
   * @param amount The amount of SUI tokens to stake.
   * @param validator The validator's Sui address.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the stake addition request is
   *   completed.
   * @throws SuiException if there is an error during the stake addition request.
   */
  suspend fun requestAddStake(
    signer: SuiAddress,
    coins: List<ObjectId>,
    amount: Long,
    validator: SuiAddress,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_requestAddStake",
        *listOf(
            signer.pubKey,
            coins.map { it.hash },
            amount.toString(),
            validator.pubKey,
            gas?.hash,
            gasBudget.toString(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Withdraw stake from a validator's staking pool.
   *
   * @param signer The transaction signer's Sui address.
   * @param stakedSui Staked Sui object ID.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the withdrawal request is
   *   completed.
   * @throws SuiException if there is an error during the withdrawal request.
   */
  suspend fun requestWithdrawStake(
    signer: SuiAddress,
    stakedSui: ObjectId,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_requestWithdrawStake",
        *listOf(signer.pubKey, stakedSui.hash, gas?.hash, gasBudget.toString()).toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to split a coin object into multiple coins.
   *
   * @param signer The transaction signer's Sui address.
   * @param coinObjectId The ID of the coin to be split.
   * @param splitAmounts The amounts to split out from the coin.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the split is completed.
   * @throws SuiException if there is an error during the split.
   */
  suspend fun splitCoin(
    signer: SuiAddress,
    coinObjectId: ObjectId,
    splitAmounts: List<Long>,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_splitCoin",
        *listOf(
            signer.pubKey,
            coinObjectId.hash,
            splitAmounts.map { it.toString() },
            gas?.hash,
            gasBudget.toString(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to split a coin object into multiple equal-size coins.
   *
   * @param signer The transaction signer's Sui address.
   * @param coinObjectId The ID of the coin to be split.
   * @param splitCount The number of coins to split into.
   * @param gas The gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @return [TransactionBlockBytes] The transaction block bytes after the split is completed.
   * @throws SuiException if there is an error during the split.
   */
  suspend fun splitCoinEqual(
    signer: SuiAddress,
    coinObjectId: ObjectId,
    splitCount: Long,
    gas: ObjectId? = null,
    gasBudget: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_splitCoinEqual",
        *listOf(
            signer.pubKey,
            coinObjectId.hash,
            splitCount.toString(),
            gas?.hash,
            gasBudget.toString(),
          )
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to transfer an object from one address to another. The object's
   * type must allow public transfers.
   *
   * @param signer The transaction signer's Sui address.
   * @param objectId The ID of the object being transferred.
   * @param gas object to be used in this transaction, node will pick one from the signer's
   *   possession if not provided
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @param recipient The SUI address of the recipient account.
   * @return [TransactionBlockBytes] The transaction block bytes after the transfer is completed.
   * @throws SuiException if there is an error during the transfer.
   */
  suspend fun transferObject(
    signer: SuiAddress,
    objectId: ObjectId,
    gas: ObjectId? = null,
    gasBudget: Long,
    recipient: SuiAddress,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_transferObject",
        *listOf(signer.pubKey, objectId.hash, gas?.hash, gasBudget.toString(), recipient.pubKey)
          .toTypedArray(),
      )
    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }

  /**
   * Create an unsigned transaction to send SUI coin object to a Sui address. The SUI object is also
   * used as the gas object.
   *
   * @param signer The transaction signer's Sui address.
   * @param suiObjectId The Sui coin object to be used in this transaction.
   * @param gasBudget The gas budget, the transaction will fail if the gas cost exceed the budget.
   * @param recipient The SUI address of the recipient account.
   * @param amount The amount to be split out and transferred.
   * @return [TransactionBlockBytes] The transaction block bytes after the transfer is completed.
   * @throws SuiException if there is an error during the transfer.
   */
  suspend fun transferSui(
    signer: SuiAddress,
    suiObjectId: ObjectId,
    gasBudget: Long,
    recipient: SuiAddress,
    amount: Long,
  ): TransactionBlockBytes {
    val resp =
      call<Response<TransactionBlockBytes>>(
        "unsafe_transferSui",
        *listOf(
            signer.pubKey,
            suiObjectId.hash,
            gasBudget.toString(),
            recipient.pubKey,
            amount.toString(),
          )
          .toTypedArray(),
      )

    when (resp) {
      is Response.Ok -> return resp.data
      is Response.Error -> throw SuiException(resp.message)
    }
  }
}
