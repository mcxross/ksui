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
  vararg inputs: T,
): List<Argument> = inputs.map { it as? Argument.Result ?: input(it) }

inline fun <reified T : TypeTag> ProgrammableTransactionBuilder.types(
  vararg types: T,
): List<TypeTag> = types.toList()

/**
 * Parses a Move type such as `u64`, `vector<u8>` or `0x2::coin::Coin<0x2::sui::SUI>`, including
 * nested type arguments. Whitespace between tokens is ignored; anything else that is not a complete
 * type fails rather than being dropped.
 *
 * @throws IllegalArgumentException if the string is not a complete Move type.
 */
@Throws(IllegalArgumentException::class)
fun String.toTypeTag(): TypeTag = MoveTypeParser(this).parse()

/** Recursive-descent parser for Move type strings, following move-core-types' grammar. */
private class MoveTypeParser(private val text: String) {
  private var at = 0

  fun parse(): TypeTag {
    val tag = typeTag()
    skipSpaces()
    if (at != text.length) fail("unexpected '${text.substring(at)}'")
    return tag
  }

  private fun fail(reason: String): Nothing =
    throw IllegalArgumentException("Could not parse TypeTag '$text': $reason")

  private fun skipSpaces() {
    while (at < text.length && text[at].isWhitespace()) at++
  }

  private fun peek(token: String): Boolean {
    skipSpaces()
    return text.startsWith(token, at)
  }

  private fun expect(token: String) {
    if (!peek(token)) fail("expected '$token' at ${at + 1}")
    at += token.length
  }

  private fun word(): String {
    skipSpaces()
    val start = at
    while (at < text.length && (text[at].isLetterOrDigit() || text[at] == '_')) at++
    if (at == start) fail("expected a type at ${start + 1}")
    return text.substring(start, at)
  }

  private fun typeTag(): TypeTag =
    when (val word = word()) {
      "bool" -> TypeTag.Bool
      "u8" -> TypeTag.U8
      "u16" -> TypeTag.U16
      "u32" -> TypeTag.U32
      "u64" -> TypeTag.U64
      "u128" -> TypeTag.U128
      "u256" -> TypeTag.U256
      "address" -> TypeTag.Address
      "signer" -> TypeTag.Signer
      "vector" -> {
        expect("<")
        val element = typeTag()
        expect(">")
        TypeTag.Vector(element)
      }
      else -> TypeTag.Struct(structTag(word))
    }

  private fun structTag(addressText: String): StructTag {
    val address =
      try {
        AccountAddress.fromString(addressText)
      } catch (e: IllegalArgumentException) {
        fail("'$addressText' is neither a primitive type nor an address")
      }
    expect("::")
    val module = identifier()
    expect("::")
    val name = identifier()
    val typeParams = mutableListOf<TypeTag>()
    if (peek("<")) {
      expect("<")
      typeParams += typeTag()
      while (peek(",")) {
        expect(",")
        typeParams += typeTag()
      }
      expect(">")
    }
    return StructTag(address, module, name, typeParams)
  }

  // move-core-types: a letter then letters, digits or '_', or '_' followed by at least one of them.
  private fun identifier(): String {
    val word = word()
    val valid =
      (word[0].isAsciiLetter() || (word[0] == '_' && word.length > 1)) &&
        word.all { it.isAsciiLetter() || it in '0'..'9' || it == '_' }
    if (!valid) fail("'$word' is not a Move identifier")
    return word
  }

  private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
}
