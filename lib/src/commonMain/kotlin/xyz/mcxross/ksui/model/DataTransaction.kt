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

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.ptb.TransactionKind
import xyz.mcxross.ksui.serializer.TransactionInputListSerializer
import xyz.mcxross.ksui.serializer.TransactionListSerializer

@Serializable
abstract class DataTransactionInput {
  abstract val type: String

  @Serializable data class DtiDefault(override val type: String) : DataTransactionInput()

  @Serializable
  data class DtiImmOrOwnedObject(
    override val type: String,
    val objectType: String,
    val objectId: String,
    val version: Long,
    val digest: String,
  ) : DataTransactionInput()

  @Serializable
  data class DtiPure(override val type: String, val valueType: String, val value: String? = null) :
    DataTransactionInput()

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
