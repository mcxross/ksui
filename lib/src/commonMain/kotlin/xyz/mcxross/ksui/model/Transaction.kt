package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.DisassembledFieldSerializer
import xyz.mcxross.ksui.model.serializer.ObjectChangeSerializer

enum class ExecuteTransactionRequestType {
  WAITFOREFFECTSCERT {
    override fun value(): String = "WaitForEffectsCert"
  },
  WAITFORLOCALEXECUTION {
    override fun value(): String = "WaitForLocalExecution"
  };

  abstract fun value(): String
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
