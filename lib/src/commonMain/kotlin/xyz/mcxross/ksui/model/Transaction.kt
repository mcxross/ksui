package xyz.mcxross.ksui.model

import kotlin.reflect.safeCast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.exception.UnknownTransactionFilterException
import xyz.mcxross.ksui.model.serializer.CallArgObjectSerializer
import xyz.mcxross.ksui.model.serializer.CallArgPureSerializer
import xyz.mcxross.ksui.model.serializer.DisassembledFieldSerializer
import xyz.mcxross.ksui.model.serializer.ObjectChangeSerializer
import xyz.mcxross.ksui.model.serializer.TransactionFilterSerializer
import xyz.mcxross.ksui.model.serializer.TxnSubResSerializer

@Serializable data class TransactionDigest(val value: String)

@Serializable data class TransactionDigests(@SerialName("result") val vec: List<String>)

enum class ExecuteTransactionRequestType {
  WAITFOREFFECTSCERT {
    override fun value(): String = "WaitForEffectsCert"
  },
  WAITFORLOCALEXECUTION {
    override fun value(): String = "WaitForLocalExecution"
  };

  abstract fun value(): String
}

enum class TransactionBlockBuilderMode {
  Commit {
    override val value: String = "Commit"
  },
  DevInspect {
    override val value: String = "DevInspect"
  };

  abstract val value: String
}

@Serializable data class Transaction(val data: Data, val txSignatures: List<String>)

@Serializable
sealed class TransactionKind {
  @Serializable data class DefaultTransaction(val kind: String) : TransactionKind()

  @Serializable
  data class ProgrammableTransaction(
    val programmableTransaction: xyz.mcxross.ksui.model.ProgrammableTransaction
  ) : TransactionKind()

  data class MoveCall(
    @SerialName("package") val pakage: String,
    val module: String,
    val function: String,
    @SerialName("type_arguments") val typeArguments: List<String>? = emptyList(),
  ) : TransactionKind()

  @Serializable
  data class Transfer(
    val recipient: String,
    @SerialName("objectRef") val objectReference: ObjectReference,
  ) : TransactionKind()

  @Serializable
  data class PaySui(
    val coins: List<ObjectReference>,
    val recipients: List<String>,
    val amounts: List<Long>,
  ) : TransactionKind()

  @Serializable data class TransferSui(val recipient: String, val amount: Long) : TransactionKind()

  @Serializable
  data class PayAllSui(val recipient: String, val coins: List<ObjectReference>) : TransactionKind()

  @Serializable
  data class Publish(
    @Serializable(with = DisassembledFieldSerializer::class) val disassembled: Any
  ) : TransactionKind()

  @Serializable
  data class SplitCoin(@SerialName("SplitCoins") val splitCoins: List<String>) : TransactionKind()
}

@Serializable
sealed class CallArg {

  @Serializable
  @SerialName("")
  data class Pure(val data: ByteArray) : CallArg() {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false

      other as Pure

      return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
      return data.contentHashCode()
    }
  }

  @Serializable(with = CallArgObjectSerializer::class)
  data class Object(val arg: ObjectArg) : CallArg()
}

@Serializable
sealed class ObjectArg {

  @Serializable data class ImmOrOwnedObject(val objectRef: ObjectReference) : ObjectArg()

  @Serializable
  data class SharedObject(val id: ObjectId, val initialSharedVersion: Long, val mutable: Boolean) :
    ObjectArg()
}

@Serializable
data class TransactionBlockResponse(
  val digest: String,
  val transaction: Transaction? = null,
  val rawTransaction: String = "",
  val effects: Effects? = null,
  val events: List<Event> = emptyList(),
  @Serializable(with = ObjectChangeSerializer::class)
  val objectChanges: List<ObjectChange> = emptyList(),
  val balanceChanges: List<BalanceChange> = emptyList(),
  val timestampMs: Long,
  val checkpoint: Long,
  val errors: List<String> = emptyList(),
)

/** The options for the transaction response. */
@Serializable
data class TransactionBlockResponseOptions(
  val showInput: Boolean,
  val showRawInput: Boolean,
  val showEffects: Boolean,
  val showEvents: Boolean,
  val showObjectChanges: Boolean,
  val showBalanceChanges: Boolean,
)

@Serializable
data class TransactionBlockResponseQueryFilter(
  @SerialName("InputObject") val inputObject: String? = null
)

/** The transaction query criteria. */
@Serializable
data class TransactionBlockResponseQuery(
  val filter: TransactionBlockResponseQueryFilter? = null,
  val options: TransactionBlockResponseOptions? = null,
)

@Serializable data class TransactionBlock(val digest: String)

@Serializable
data class TransactionBlocksPage(
  val data: List<TransactionBlock>,
  val nextCursor: String? = null,
  val hasNextPage: Boolean,
)

@Serializable data class TransactionBlockBytes(val txBytes: String, val gas: List<Gas>)

@Serializable data class Input(val type: String, val valueType: String, val value: String)

// TODO: Complete impl
@Serializable data class Transaction1(val kind: String, val inputs: List<Input>)

@Serializable
data class TransactionBlockData(
  val messageVersion: String,
  val transaction: Transaction1,
  val sender: String,
  val gasData: GasData,
)

@Serializable
data class DryRunTransactionBlockResponse(
  val effects: TransactionBlockEffects,
  val events: List<Event>,
  @Serializable(with = ObjectChangeSerializer::class) val objectChanges: List<ObjectChange>,
  @Serializable(with = ObjectChangeSerializer::class) val balanceChanges: List<BalanceChange>,
  val input: TransactionBlockData,
)

@Serializable(with = TxnSubResSerializer::class)
sealed class TransactionSubscriptionResponse {
  data class Ok(val subscriptionId: Long) : TransactionSubscriptionResponse()

  data class Effect(val effect: TransactionBlockEffects) : TransactionSubscriptionResponse()

  data class Error(val code: Int, val message: String) : TransactionSubscriptionResponse()
}

@Serializable(with = TransactionFilterSerializer::class)
open class TransactionFilter {
  @Serializable data class Checkpoint(val checkpointSequenceNumber: Long) : TransactionFilter()

  @Serializable
  data class MoveFunction(val pakage: ObjectId, val module: String, val function: String) :
    TransactionFilter()

  @Serializable data class InputObject(val objectId: ObjectId) : TransactionFilter()

  @Serializable data class ChangedObject(val objectId: ObjectId) : TransactionFilter()

  @Serializable data class FromAddress(val address: SuiAddress) : TransactionFilter()

  @Serializable data class ToAddress(val address: SuiAddress) : TransactionFilter()

  @Serializable
  data class FromAndToAddress(val fromAddress: SuiAddress, val toAddress: SuiAddress) :
    TransactionFilter()

  @Serializable data class FromOrToAddress(val suiAddress: SuiAddress) : TransactionFilter()
}

abstract class TransactionFilterMutable {

  abstract fun toImmutable(): TransactionFilter

  @Serializable
  class Checkpoint(var checkpointSequenceNumber: Long = -1) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter {
      return TransactionFilter.Checkpoint(checkpointSequenceNumber)
    }
  }

  @Serializable
  data class MoveFunction(
    var pakage: ObjectId = ObjectId(""),
    var module: String = "",
    var function: String = "",
  ) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter =
      TransactionFilter.MoveFunction(pakage, module, function)
  }

  @Serializable
  data class InputObject(var objectId: ObjectId = ObjectId("")) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter = TransactionFilter.InputObject(objectId)
  }

  @Serializable
  data class ChangedObject(var objectId: ObjectId = ObjectId("")) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter = TransactionFilter.ChangedObject(objectId)
  }

  @Serializable
  data class FromAddress(var address: SuiAddress = SuiAddress("")) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter = TransactionFilter.FromAddress(address)
  }

  @Serializable
  data class ToAddress(var address: SuiAddress = SuiAddress("")) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter = TransactionFilter.ToAddress(address)
  }

  @Serializable
  data class FromAndToAddress(
    var fromAddress: SuiAddress = SuiAddress(""),
    var toAddress: SuiAddress = SuiAddress(""),
  ) : TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter =
      TransactionFilter.FromAndToAddress(fromAddress, toAddress)
  }

  @Serializable
  data class FromOrToAddress(var suiAddress: SuiAddress = SuiAddress("")) :
    TransactionFilterMutable() {
    override fun toImmutable(): TransactionFilter = TransactionFilter.FromOrToAddress(suiAddress)
  }
}

inline fun <reified T : TransactionFilterMutable> transactionFilterFor(
  block: T.() -> Unit
): TransactionFilter =
  when (T::class) {
    TransactionFilterMutable.Checkpoint::class -> {
      val checkpointMutable =
        T::class.safeCast(TransactionFilterMutable.Checkpoint())!!.apply(block)
      checkpointMutable.toImmutable()
    }
    TransactionFilterMutable.MoveFunction::class -> {
      val moveFunctionMutable =
        T::class.safeCast(TransactionFilterMutable.MoveFunction())!!.apply(block)
      moveFunctionMutable.toImmutable()
    }
    TransactionFilterMutable.InputObject::class -> {
      val inputObjectMutable =
        T::class.safeCast(TransactionFilterMutable.InputObject())!!.apply(block)
      inputObjectMutable.toImmutable()
    }
    TransactionFilterMutable.ChangedObject::class -> {
      val changedObjectMutable =
        T::class.safeCast(TransactionFilterMutable.ChangedObject())!!.apply(block)
      changedObjectMutable.toImmutable()
    }
    TransactionFilterMutable.FromAddress::class -> {
      val fromAddressMutable =
        T::class.safeCast(TransactionFilterMutable.FromAddress())!!.apply(block)
      fromAddressMutable.toImmutable()
    }
    TransactionFilterMutable.ToAddress::class -> {
      val toAddressMutable = T::class.safeCast(TransactionFilterMutable.ToAddress())!!.apply(block)
      toAddressMutable.toImmutable()
    }
    TransactionFilterMutable.FromAndToAddress::class -> {
      val fromAndToAddressMutable =
        T::class.safeCast(TransactionFilterMutable.FromAndToAddress())!!.apply(block)
      fromAndToAddressMutable.toImmutable()
    }
    TransactionFilterMutable.FromOrToAddress::class -> {
      val fromOrToAddressMutable =
        T::class.safeCast(TransactionFilterMutable.FromOrToAddress())!!.apply(block)
      fromOrToAddressMutable.toImmutable()
    }
    else -> throw UnknownTransactionFilterException("Unknown TransactionFilter type: ${T::class}")
  }

object TransactionDataComposer {
  fun programmable(
    sender: SuiAddress,
    gapPayment: List<ObjectReference>,
    pt: ProgrammableTransaction,
    gasBudget: ULong,
    gasPrice: ULong,
  ): TransactionData = programmableAllowSponsor(sender, gapPayment, pt, gasBudget, gasPrice, sender)

  fun programmableAllowSponsor(
    sender: SuiAddress,
    gapPayment: List<ObjectReference>,
    pt: ProgrammableTransaction,
    gasBudget: ULong,
    gasPrice: ULong,
    sponsor: SuiAddress,
  ): TransactionData =
    withGasCoinsAllowSponsor(
      TransactionKind.ProgrammableTransaction(pt),
      sender,
      gapPayment,
      gasBudget,
      gasPrice,
      sponsor,
    )

  fun withGasCoinsAllowSponsor(
    kind: TransactionKind,
    sender: SuiAddress,
    gapPayment: List<ObjectReference>,
    gasBudget: ULong,
    gasPrice: ULong,
    sponsor: SuiAddress,
  ): TransactionData =
    TransactionData.V1(
      TransactionDataV1(
        kind,
        sender,
        GasData(gapPayment, sponsor.pubKey, gasBudget, gasPrice),
        TransactionExpiration.None,
      )
    )
}

@Serializable
sealed class TransactionData {
  abstract fun toBcs(): ByteArray

  @Serializable
  data class V1(val data: TransactionDataV1) : TransactionData() {
    override fun toBcs(): ByteArray = Bcs.encodeToByteArray(data)
  }
}

@Serializable
data class TransactionDataV1(
  val kind: TransactionKind,
  val sender: SuiAddress,
  val gasData: GasData,
  val expiration: TransactionExpiration,
) : TransactionDataVersion

interface TransactionDataVersion

@Serializable
sealed class TransactionExpiration {
  @Serializable data object None : TransactionExpiration()

  @Serializable data class Epoch(val epochId: EpochId) : TransactionExpiration()
}

@Serializable
open class Command {

  private val commands: MutableList<Command> = mutableListOf()
  val list: List<Command>
    get() = commands

  /**
   * A call to either an entry or a public Move function.
   *
   * @param moveCall to call.
   */
  @Serializable data class MoveCall(val moveCall: ProgrammableMoveCall) : Command()

  /**
   * Sends n-objects to the specified address.
   *
   * These objects must have store (public transfer) and either the previous owner must be an
   * address or the object must be newly created.
   *
   * @param objects to send.
   * @param address to send the objects to.
   */
  @Serializable
  @SerialName("")
  data class TransferObjects(val objects: List<Argument>, val address: Argument) : Command()

  /**
   * Splits off some amounts into a new coins with those amounts.
   *
   * @param coin to split.
   * @param into the amounts to split the coin into.
   */

  @Serializable
  @SerialName("")
  data class SplitCoins(val coin: Argument, val into: List<Argument>) : Command()

  /**
   * Merges n-coins into the first coin
   *
   * @param coin to merge into.
   * @param coins to merge.
   */
  @Serializable data class MergeCoins(val coin: Argument, val coins: List<Argument>) : Command()

  /**
   * Publishes a Move package.
   *
   * It takes the package bytes and a list of the package's transitive dependencies to link against
   * on-chain.
   *
   * @param bytes of the module.
   * @param dependencies of the module.
   */
  @Serializable
  data class Publish(val bytes: List<List<Byte>>, val dependencies: List<ObjectId>) : Command()

  /**
   * Given n-values of the same type, it constructs a vector.
   *
   * For non objects or an empty vector, the type tag must be specified.
   *
   * @param typeTag of the values.
   * @param values to create the vector from.
   */
  @Serializable
  data class MakeMoveVec(val typeTag: TypeTag?, val values: List<Argument>) : Command()

  /**
   * Upgrades a Move package.
   *
   * Takes (in order):
   * 1. A vector of serialized modules for the package.
   * 2. A vector of object ids for the transitive dependencies of the new package.
   * 3. The object ID of the package being upgraded.
   * 4. An argument holding the `UpgradeTicket` that must have been produced from an earlier command
   *    in the same programmable transaction.
   *
   * @param modules of the package.
   * @param dependencies of the package.
   * @param packageId of the package.
   * @param upgradeTicket of the package.
   */
  @Serializable
  data class Upgrade(
    val modules: List<List<Byte>>,
    val dependencies: List<ObjectId>,
    val packageId: ObjectId,
    val upgradeTicket: Argument,
  ) : Command()

  /**
   * Creates a [MoveCall] command.
   *
   * It requires that the [MoveCallBuilder.moveCall] is provided otherwise it will throw an
   * [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [MoveCall] command.
   */
  fun moveCall(block: MoveCallBuilder.() -> Unit): MoveCall {
    val builder = MoveCallBuilder().apply(block)
    require(builder.isValid()) { "moveCall must not be null" }
    return MoveCall(builder.moveCall!!)
  }

  /**
   * Creates a [TransferObjects] command.
   *
   * It requires that the [TransferObjectsBuilder.objects] and [TransferObjectsBuilder.to] are
   * provided otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun transferObjects(block: TransferObjectsBuilder.() -> Unit): Argument.Result {
    val builder = TransferObjectsBuilder().apply(block)
    require(builder.isValid()) { "objects and to must not be empty" }
    commands.add(TransferObjects(builder.objects, builder.to!!))
    return Argument.Result(commands.size - 1)
  }

  /**
   * Creates a [SplitCoins] command.
   *
   * It requires that the [SplitCoinsBuilder.coin] and [SplitCoinsBuilder.into] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun splitCoins(block: SplitCoinsBuilder.() -> Unit): Argument.Result {
    val builder = SplitCoinsBuilder().apply(block)
    require(builder.isValid()) { "coin and into must not be empty" }
    commands.add(SplitCoins(builder.coin!!, builder.into))
    return Argument.Result(commands.size - 1)
  }

  /**
   * Creates a [MergeCoins] command.
   *
   * It requires that the [MergeCoinsBuilder.coin] and [MergeCoinsBuilder.coins] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun mergeCoins(block: MergeCoinsBuilder.() -> Unit): Argument.Result {
    val builder = MergeCoinsBuilder().apply(block)
    require(builder.isValid()) { "coin and coins must not be empty" }
    commands.add(MergeCoins(builder.coin!!, builder.coins))
    return Argument.Result(commands.size - 1)
  }

  /**
   * Creates a [Publish] command.
   *
   * It requires that the [PublishBuilder.bytes] and [PublishBuilder.dependencies] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun publish(block: PublishBuilder.() -> Unit): Argument.Result {
    val builder = PublishBuilder().apply(block)
    require(builder.isValid()) { "bytes and dependencies must not be empty" }
    commands.add(Publish(builder.bytes, builder.dependencies))
    return Argument.Result(commands.size - 1)
  }

  /**
   * Creates a [MakeMoveVec] command.
   *
   * It requires that the [MakeMoveVecBuilder.typeTag] and [MakeMoveVecBuilder.values] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun makeMoveVec(block: MakeMoveVecBuilder.() -> Unit): Argument.Result {
    val builder = MakeMoveVecBuilder().apply(block)
    require(builder.isValid()) { "typeTag and values must not be empty" }
    commands.add(MakeMoveVec(builder.typeTag, builder.values))
    return Argument.Result(commands.size - 1)
  }

  /**
   * Creates a [Upgrade] command.
   *
   * It requires that the [UpgradeBuilder.modules], [UpgradeBuilder.dependencies],
   * [UpgradeBuilder.packageId], and [UpgradeBuilder.upgradeTicket] are provided otherwise it will
   * throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun upgrade(block: UpgradeBuilder.() -> Unit): Argument.Result {
    val builder = UpgradeBuilder().apply(block)
    require(builder.isValid()) {
      "modules, dependencies, packageId, and upgradeTicket must not be empty"
    }
    commands.add(
      Upgrade(builder.modules, builder.dependencies, builder.packageId, builder.upgradeTicket)
    )
    return Argument.Result(commands.size - 1)
  }
}

interface Builder {
  fun isValid(): Boolean
}

class MoveCallBuilder : Builder {
  var moveCall: ProgrammableMoveCall? = null

  override fun isValid(): Boolean = moveCall != null
}

class TransferObjectsBuilder : Builder {
  lateinit var objects: List<Argument>
  var to: Argument? = null

  override fun isValid(): Boolean = to != null && objects.isNotEmpty()
}

class SplitCoinsBuilder : Builder {
  var coin: Argument? = null
  lateinit var into: List<Argument>

  override fun isValid(): Boolean = coin != null && into.isNotEmpty()
}

class MergeCoinsBuilder : Builder {
  var coin: Argument? = null
  lateinit var coins: List<Argument>

  override fun isValid(): Boolean = coin != null && coins.isNotEmpty()
}

class PublishBuilder : Builder {
  lateinit var bytes: List<List<Byte>>
  lateinit var dependencies: List<ObjectId>

  override fun isValid(): Boolean = dependencies.isNotEmpty() && bytes.isNotEmpty()
}

class MakeMoveVecBuilder : Builder {
  var typeTag: TypeTag? = null
  lateinit var values: List<Argument>

  override fun isValid(): Boolean = typeTag != null && values.isNotEmpty()
}

class UpgradeBuilder : Builder {
  lateinit var modules: List<List<Byte>>
  lateinit var dependencies: List<ObjectId>
  lateinit var packageId: ObjectId
  lateinit var upgradeTicket: Argument

  override fun isValid(): Boolean {
    TODO("Not yet implemented")
  }
}

// TODO: for module and function params, check on `Identifier` type
@Serializable
data class ProgrammableMoveCall(
  val pakage: ObjectId,
  val module: String,
  val function: String,
  val typeArguments: List<TypeTag>,
  val arguments: List<Argument>,
)

@Serializable
sealed class Argument {

  @Serializable data object GasCoin : Argument()

  @Serializable data class Input <T> (val inputObjectOrPrimitiveValue: T) : Argument()

  @Serializable data class Result(val commandResult: Int) : Argument()

  @Serializable
  data class NestedResult(val commandIndex: Int, val returnValueIndex: Int) : Argument()
}

@Serializable
data class SenderSignedData(val senderSignedTransactions: List<SenderSignedTransaction>)

@Serializable
data class SenderSignedTransaction(
  val intentMessage: IntentMessage<TransactionData>,
  val txSignatures: List<String>,
)
