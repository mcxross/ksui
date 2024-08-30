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
package xyz.mcxross.ksui.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.TransactionKind
import xyz.mcxross.ksui.serializer.CallArgObjectSerializer
import xyz.mcxross.ksui.serializer.PureSerializer
import xyz.mcxross.ksui.serializer.TransactionExpirationSerializer
import xyz.mcxross.ksui.serializer.TransactionFilterSerializer
import xyz.mcxross.ksui.serializer.V1Serializer
import xyz.mcxross.ksui.util.bcsEncode

@Serializable data class TransactionDigest(val value: String)

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
sealed class CallArg {

  @Serializable(with = PureSerializer::class)
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

@Serializable(with = TransactionFilterSerializer::class)
open class TransactionFilter {
  @Serializable data class Checkpoint(val checkpointSequenceNumber: Long) : TransactionFilter()

  @Serializable
  data class MoveFunction(val pakage: ObjectId, val module: String, val function: String) :
    TransactionFilter()

  @Serializable data class InputObject(val objectId: ObjectId) : TransactionFilter()

  @Serializable data class ChangedObject(val objectId: ObjectId) : TransactionFilter()

  @Serializable data class FromAddress(val address: AccountAddress) : TransactionFilter()

  @Serializable data class ToAddress(val address: AccountAddress) : TransactionFilter()

  @Serializable
  data class FromAndToAddress(val fromAddress: AccountAddress, val toAddress: AccountAddress) :
    TransactionFilter()

  @Serializable data class FromOrToAddress(val suiAddress: AccountAddress) : TransactionFilter()
}

object TransactionDataComposer {
  fun programmable(
    sender: AccountAddress,
    gapPayment: List<ObjectReference>,
    pt: ProgrammableTransaction,
    gasBudget: ULong,
    gasPrice: ULong,
  ): TransactionData = programmableAllowSponsor(sender, gapPayment, pt, gasBudget, gasPrice, sender)

  fun programmableAllowSponsor(
    sender: AccountAddress,
    gapPayment: List<ObjectReference>,
    pt: ProgrammableTransaction,
    gasBudget: ULong,
    gasPrice: ULong,
    sponsor: AccountAddress,
  ): TransactionData =
    withGasCoinsAllowSponsor(
      kind = TransactionKind.ProgrammableTransaction(pt),
      sender = sender,
      gapPayment = gapPayment,
      gasBudget = gasBudget,
      gasPrice = gasPrice,
      sponsor = sponsor,
    )

  fun withGasCoinsAllowSponsor(
    kind: TransactionKind,
    sender: AccountAddress,
    gapPayment: List<ObjectReference>,
    gasBudget: ULong,
    gasPrice: ULong,
    sponsor: AccountAddress,
  ): TransactionData =
    TransactionData.V1(
      TransactionDataV1(
        kind = kind,
        sender = sender,
        gasData = GasData(payment = gapPayment, owner = sponsor, price = gasPrice, budget = gasBudget),
        expiration = TransactionExpiration.None,
      )
    )
}

@Serializable(with = V1Serializer::class)
sealed class TransactionData {
  abstract fun toBcs(): ByteArray

  companion object {

    fun programmable(
      sender: String,
      gasPayment: List<ObjectReference>,
      pt: ProgrammableTransaction,
      gasBudget: ULong,
      gasPrice: ULong,
    ): TransactionData = programmable(AccountAddress(sender), gasPayment, pt, gasBudget, gasPrice)

    fun programmable(
      sender: AccountAddress,
      gasPayment: List<ObjectReference>,
      pt: ProgrammableTransaction,
      gasBudget: ULong,
      gasPrice: ULong,
    ): TransactionData =
      programmableAllowSponsor(sender, gasPayment, pt, gasBudget, gasPrice, sender)

    fun programmableAllowSponsor(
      sender: AccountAddress,
      gasPayment: List<ObjectReference>,
      pt: ProgrammableTransaction,
      gasBudget: ULong,
      gasPrice: ULong,
      gasSponsor: AccountAddress,
    ): TransactionData {
      val kind = (TransactionKind::ProgrammableTransaction)(pt)
      return newWithGasCoinsAllowSponsor(kind, sender, gasPayment, gasBudget, gasPrice, gasSponsor)
    }

    fun newWithGasCoinsAllowSponsor(
      kind: TransactionKind,
      sender: AccountAddress,
      gasPayment: List<ObjectReference>,
      gasBudget: ULong,
      gasPrice: ULong,
      gasSponsor: AccountAddress,
    ): TransactionData =
      (TransactionData::V1)(
        TransactionDataV1(
          kind = kind,
          sender = sender,
          gasData = GasData(gasPayment, gasSponsor, gasPrice, gasBudget),
          expiration = TransactionExpiration.None,
        )
      )
  }

  @Serializable
  data class V1(val data: TransactionDataV1) : TransactionData() {
    override fun toBcs(): ByteArray = Bcs.encodeToByteArray(data)
  }
}

// TODO: This is a placeholder for now, look back at this later

enum class B{
  A
}

@Serializable
data class TransactionDataV1(
  val a : B = B.A,
  val kind: TransactionKind,
  val sender: AccountAddress,
  val gasData: GasData,
  val expiration: TransactionExpiration,
) : TransactionDataVersion

interface TransactionDataVersion

@Serializable(with = TransactionExpirationSerializer::class)
sealed class TransactionExpiration {
  @Serializable data object None : TransactionExpiration()

  @Serializable data class Epoch(val epoch: xyz.mcxross.ksui.model.Epoch) : TransactionExpiration()
}

@Serializable
data class SenderSignedData(val senderSignedTransactions: List<SenderSignedTransaction>)

@Serializable
data class SenderSignedTransaction(
  val intentMessage: IntentMessage<TransactionData>,
  val txSignatures: List<String>,
)

typealias Txn = Envelope<SenderSignedData>

fun TransactionData.toTransaction(txSignatures: List<String>): Txn =
  Envelope(
    SenderSignedData(
      listOf(SenderSignedTransaction(IntentMessage(Intent.suiTransaction(), this), txSignatures))
    )
  )

infix fun TransactionData.with(txSignatures: List<String>): Txn = toTransaction(txSignatures)

@OptIn(ExperimentalEncodingApi::class) fun Txn.data(): String = Base64.encode(bcsEncode(this.data.senderSignedTransactions[0].intentMessage.value))

fun Txn.signatures(): List<String> = this.data.senderSignedTransactions.first().txSignatures

fun Txn.content(): Pair<String, List<String>> = this.data() to this.signatures()
