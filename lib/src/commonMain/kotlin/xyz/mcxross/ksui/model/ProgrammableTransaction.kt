package xyz.mcxross.ksui.model

class ProgrammableTransactionBuilder {
  private val inputs: MutableMap<BuilderArg, CallArg> = LinkedHashMap()
  private val commands: MutableList<Command> = mutableListOf()

  fun input(arg: BuilderArg, value: CallArg) {
    inputs[arg] = value
  }

  fun command(command: Command) {
    commands.add(command)
  }

  fun build(): TransactionKind.ProgrammableTransaction {
    return TransactionKind.ProgrammableTransaction(inputs.values.toList(), commands)
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

/** A DSL for building a [TransactionKind.ProgrammableTransaction]. */
fun programmableTx(
  block: ProgrammableTransactionBuilder.() -> Unit
): TransactionKind.ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  builder.block()
  return builder.build()
}
