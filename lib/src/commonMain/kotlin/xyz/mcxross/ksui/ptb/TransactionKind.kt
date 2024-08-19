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
package xyz.mcxross.ksui.ptb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.serializer.DisassembledFieldSerializer
import xyz.mcxross.ksui.serializer.TransactionKindSerializer

@Serializable(with = TransactionKindSerializer::class)
sealed class TransactionKind {
  @Serializable data class DefaultTransaction(val kind: String) : TransactionKind()

  @Serializable
  data class ProgrammableTransaction(val pt: xyz.mcxross.ksui.ptb.ProgrammableTransaction) :
    TransactionKind()

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
