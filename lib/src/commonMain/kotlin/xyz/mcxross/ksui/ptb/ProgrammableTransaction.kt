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

import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.serializer.AnySerializer

@Serializable
data class ProgrammableTransaction(
  val inputs: List<@Serializable(with = AnySerializer::class) Any>,
  val commands: List<@Serializable(with = AnySerializer::class) Any>,
) : TransactionKind()

class ProgrammableTransactionBuilder {
  private val inputs: MutableMap<BuilderArg, CallArg> = mutableMapOf()
  private val command = Command()

  private fun input(arg: BuilderArg, value: CallArg): Argument {
    inputs[arg] = value
    return Argument.Input((inputs.size - 1).toUShort())
  }

  fun input(bytes: ByteArray, forceSeparate: Boolean): Argument {
    val arg =
      if (forceSeparate) {
        BuilderArg.ForcedNonUniquePure(inputs.size)
      } else {
        BuilderArg.Pure(bytes)
      }
    return input(arg, CallArg.Pure(data = bytes))
  }

  inline fun <reified T> input(value: T): Argument {

    if (value is ObjectArg) {
      return `object`(value)
    }

    return input(Bcs.encodeToByteArray(value), false)
  }

  fun `object`(objectArg: ObjectArg): Argument {
    return input(BuilderArg.Object(objectArg.toString()), CallArg.Object(objectArg))
  }

  inline fun <reified T> forceSeparateInput(value: T): Argument {
    val bcs = Bcs {}
    return input(bcs.encodeToByteArray(value), true)
  }

  fun command(block: Command.() -> Unit) {
    command.block()
  }

  fun build(): ProgrammableTransaction {
    return ProgrammableTransaction(inputs.values.toList(), command.list)
  }
}

@Serializable
sealed class BuilderArg {

  @Serializable data class Object(val id: String) : BuilderArg()

  @Serializable
  data class Pure(val data: ByteArray) : BuilderArg() {
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

  data class ForcedNonUniquePure(val index: Int) : BuilderArg()
}

/** A DSL for building a [ProgrammableTransaction]. */
fun programmableTx(block: ProgrammableTransactionBuilder.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  builder.block()
  return builder.build()
}
