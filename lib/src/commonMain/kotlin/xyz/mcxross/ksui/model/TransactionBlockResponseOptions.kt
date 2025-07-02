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

data class TransactionBlockResponseOptions(
  val first: Int? = null,
  val last: Int? = null,
  val before: String? = null,
  val after: String? = null,
  val showBalanceChanges: Boolean? = null,
  val showEffects: Boolean? = null,
  val showRawEffects: Boolean? = null,
  val showEvents: Boolean? = null,
  val showInput: Boolean? = null,
  val showObjectChanges: Boolean? = null,
  val showRawInput: Boolean? = null,
)

data class ExecuteTransactionBlockResponseOptions(
  val showBalanceChanges: Boolean = false,
  val showEffects: Boolean = false,
  val showRawEffects: Boolean = false,
  val showEvents: Boolean = false,
  val showInput: Boolean = false,
  val showObjectChanges: Boolean = false,
  val showRawInput: Boolean = false,
)
