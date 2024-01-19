package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs

@Serializable
data class ProgrammableTransaction(val inputs: List<CallArg>, val commands: List<Command>) :
  TransactionKind()

class ProgrammableTransactionBuilder {
  private val inputs: MutableMap<BuilderArg, CallArg> = LinkedHashMap()
  private val command = Command()

  private fun input(arg: BuilderArg, value: CallArg): Argument.Input {
    inputs[arg] = value
    return Argument.Input(inputs.size - 1)
  }

  fun input(bytes: ByteArray, forceSeparate: Boolean): Argument.Input {
    val arg =
      if (forceSeparate) {
        BuilderArg.ForcedNonUniquePure(inputs.size)
      } else {
        BuilderArg.Pure(bytes)
      }
    return input(arg, CallArg.Pure(bytes))
  }

  inline fun <reified T> input(value: T): Argument.Input {
    val bcs = Bcs {}
    return input(bcs.encodeToByteArray(value), false)
  }

  inline fun <reified T> forceSeparateInput(value: T): Argument.Input {
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

sealed class BuilderArg {
  data class Object(val objectId: ObjectId) : BuilderArg()

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
