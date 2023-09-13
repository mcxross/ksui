package xyz.mcxross.ksui.model

import kotlin.reflect.safeCast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.exception.UnknownTransactionFilterException
import xyz.mcxross.ksui.model.serializer.DisassembledFieldSerializer
import xyz.mcxross.ksui.model.serializer.ObjectChangeSerializer
import xyz.mcxross.ksui.model.serializer.TransactionFilterSerializer
import xyz.mcxross.ksui.model.serializer.TxnSubResSerializer

@Serializable data class TransactionDigest(val value: String)

@Serializable
data class TransactionDigests(
  @SerialName("result")
  val vec: List<String>
)


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

@Serializable
data class Transaction(
    val data: Data,
    val txSignatures: List<String>,
)

@Serializable
abstract class TransactionKind {
  @Serializable
  data class DefaultTransaction(
      val kind: String,
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
      @SerialName("objectRef") val objectReference: ObjectReference
  ) : TransactionKind()

  @Serializable
  data class PaySui(
      val coins: List<ObjectReference>,
      val recipients: List<String>,
      val amounts: List<Long>,
  ) : TransactionKind()

  @Serializable
  data class TransferSui(
      val recipient: String,
      val amount: Long,
  ) : TransactionKind()

  @Serializable
  data class PayAllSui(
      val recipient: String,
      val coins: List<ObjectReference>,
  ) : TransactionKind()

  @Serializable
  data class Publish(
      @Serializable(with = DisassembledFieldSerializer::class) val disassembled: Any,
  ) : TransactionKind()

  @Serializable
  data class SplitCoin(
      @SerialName("SplitCoins") val splitCoins: List<String>,
  ) : TransactionKind()
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
    val showBalanceChanges: Boolean
)

@Serializable
data class TransactionBlockResponseQueryFilter(
    @SerialName("InputObject") val inputObject: String? = null,
)

/** The transaction query criteria. */
@Serializable
data class TransactionBlockResponseQuery(
    val filter: TransactionBlockResponseQueryFilter? = null,
    val options: TransactionBlockResponseOptions? = null,
)

@Serializable
data class TransactionBlock(
    val digest: String,
)

@Serializable
data class TransactionBlocksPage(
    val data: List<TransactionBlock>,
    val nextCursor: String? = null,
    val hasNextPage: Boolean,
)

@Serializable
data class TransactionBlockBytes(
    val txBytes: String,
    val gas: List<Gas>,
)

@Serializable data class Input(val type: String, val valueType: String, val value: String)

// TODO: Complete impl
@Serializable data class Transaction1(val kind: String, val inputs: List<Input>)

@Serializable
data class TransactionBlockData(
    val messageVersion: String,
    val transaction: Transaction1,
    val sender: String,
    val gasData: GasData
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
  data class Error(
      val code: Int,
      val message: String,
  ) : TransactionSubscriptionResponse()
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
      var function: String = ""
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
      var toAddress: SuiAddress = SuiAddress("")
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
        val toAddressMutable =
            T::class.safeCast(TransactionFilterMutable.ToAddress())!!.apply(block)
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
