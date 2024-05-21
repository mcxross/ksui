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
package xyz.mcxross.ksui.util

import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.model.TransactionDigest

/** Extension function to create a [TransactionDigest] from a [String]. */
fun String.toTxnDigest(): TransactionDigest = TransactionDigest(this)

/** Extension functions to create [Argument.Input]s from various types. */
inline fun <reified T : Any> ProgrammableTransactionBuilder.inputs(
  vararg inputs: T
): List<Argument> {
  return inputs.map { input(it) }
}
