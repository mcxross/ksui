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

public data class TransactionBlockFilter(
  val function: String? = null,
  val kind: TransactionBlockKindInput? = null,
  val afterCheckpoint: String? = null,
  val atCheckpoint: String? = null,
  val beforeCheckpoint: String? = null,
  val affectedAddress: String? = null,
  val sentAddress: String? = null,
  val inputObject: String? = null,
  val changedObject: String? = null,
  val transactionIds: List<String>? = emptyList(),
) {}
