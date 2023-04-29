package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.TransactionInputListSerializer
import xyz.mcxross.ksui.model.serializer.TransactionListSerializer

@Serializable
abstract class DataTransactionInput {
  abstract val type: String

  @Serializable
  data class DtiDefault(
      override val type: String,
  ) : DataTransactionInput()
  @Serializable
  data class DtiImmOrOwnedObject(
      override val type: String,
      val objectType: String,
      val objectId: String,
      val version: Long,
      val digest: String,
  ) : DataTransactionInput()

  @Serializable
  data class DtiPure(
      override val type: String,
      val valueType: String,
      val value: String,
  ) : DataTransactionInput()

  @Serializable
  data class DtiSharedObject(
      override val type: String,
      val objectType: String,
      val objectId: String,
      val initialSharedVersion: Long,
      val mutable: Boolean,
  ) : DataTransactionInput()
}

@Serializable
data class DataTransaction(
    val kind: String,
    @Serializable(with = TransactionInputListSerializer::class)
    val inputs: List<DataTransactionInput> = emptyList(),
    @Serializable(with = TransactionListSerializer::class)
    val transactions: List<TransactionKind> = emptyList(),
)

@Serializable
data class Data(
    val messageVersion: String,
    val transaction: DataTransaction,
    val sender: String,
    val gasData: GasData,
)
