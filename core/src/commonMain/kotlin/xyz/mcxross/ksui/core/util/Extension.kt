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
package xyz.mcxross.ksui.core.util

import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.StructTag
import xyz.mcxross.ksui.core.model.TransactionDigest
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder

/** Extension function to create a [TransactionDigest] from a [String]. */
fun String.toTxnDigest(): TransactionDigest = TransactionDigest(this)

/** Extension functions to create [Argument.Input]s from various types. */
inline fun <reified T : Any> ProgrammableTransactionBuilder.inputs(
  vararg inputs: T
): List<Argument> = inputs.map { it as? Argument.Result ?: input(it) }

inline fun <reified T : TypeTag> ProgrammableTransactionBuilder.types(
  vararg types: T
): List<TypeTag> = types.toList()

fun String.toTypeTag(): TypeTag {

  when (this) {
    "bool" -> return TypeTag.Bool
    "u8" -> return TypeTag.U8
    "u16" -> return TypeTag.U16
    "u32" -> return TypeTag.U32
    "u64" -> return TypeTag.U64
    "u128" -> return TypeTag.U128
    "u256" -> return TypeTag.U256
    "address" -> return TypeTag.Address
    "signer" -> return TypeTag.Signer
  }

  if (this.startsWith("vector<") && this.endsWith(">")) {
    val innerContent = this.substring(7, this.length - 1)
    return TypeTag.Vector(innerContent.toTypeTag())
  }

  val parts = this.split("::")
  if (parts.size >= 3) {
    val address = parts[0]
    val module = parts[1]
    val namePart = parts.subList(2, parts.size).joinToString("::")
    val name = namePart.substringBefore("<")

    return TypeTag.Struct(
      StructTag(
        address = AccountAddress.fromString(address),
        module = module,
        name = name,
        typeParams = emptyList(),
      )
    )
  }

  throw IllegalArgumentException("Could not parse TypeTag: $this")
}
