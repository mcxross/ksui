package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.ArgumentSerializer
import xyz.mcxross.ksui.model.serializer.CommandSerializer

@Serializable(with = CommandSerializer::class)
open class Command {

  private val commands: MutableList<Command> = mutableListOf()
  val list: List<Command>
    get() = commands

  /**
   * A call to either an entry or a public Move function.
   *
   * @param moveCall to call.
   */
  @Serializable data class MoveCall(val moveCall: ProgrammableMoveCall) : Command()

  /**
   * Sends n-objects to the specified address.
   *
   * These objects must have store (public transfer) and either the previous owner must be an
   * address or the object must be newly created.
   *
   * @param objects to send.
   * @param address to send the objects to.
   */
  @Serializable
  data class TransferObjects(val objects: List<Argument>, val address: Argument) : Command()

  /**
   * Splits off some amounts into a new coins with those amounts.
   *
   * @param coin to split.
   * @param into the amounts to split the coin into.
   */
  @Serializable data class SplitCoins(val coin: Argument, val into: List<Argument>) : Command()

  /**
   * Merges n-coins into the first coin
   *
   * @param coin to merge into.
   * @param coins to merge.
   */
  @Serializable data class MergeCoins(val coin: Argument, val coins: List<Argument>) : Command()

  /**
   * Publishes a Move package.
   *
   * It takes the package bytes and a list of the package's transitive dependencies to link against
   * on-chain.
   *
   * @param bytes of the module.
   * @param dependencies of the module.
   */
  @Serializable
  data class Publish(val bytes: List<List<Byte>>, val dependencies: List<ObjectId>) : Command()

  /**
   * Given n-values of the same type, it constructs a vector.
   *
   * For non objects or an empty vector, the type tag must be specified.
   *
   * @param typeTag of the values.
   * @param values to create the vector from.
   */
  @Serializable
  data class MakeMoveVec(val typeTag: TypeTag?, val values: List<Argument>) : Command()

  /**
   * Upgrades a Move package.
   *
   * Takes (in order):
   * 1. A vector of serialized modules for the package.
   * 2. A vector of object ids for the transitive dependencies of the new package.
   * 3. The object ID of the package being upgraded.
   * 4. An argument holding the `UpgradeTicket` that must have been produced from an earlier command
   *    in the same programmable transaction.
   *
   * @param modules of the package.
   * @param dependencies of the package.
   * @param packageId of the package.
   * @param upgradeTicket of the package.
   */
  @Serializable
  data class Upgrade(
    val modules: List<List<Byte>>,
    val dependencies: List<ObjectId>,
    val packageId: ObjectId,
    val upgradeTicket: Argument,
  ) : Command()

  /**
   * Creates a [MoveCall] command.
   *
   * It requires that the [MoveCallBuilder.moveCall] is provided otherwise it will throw an
   * [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [MoveCall] command.
   */
  fun moveCall(block: MoveCallBuilder.() -> Unit): MoveCall {
    val builder = MoveCallBuilder().apply(block)
    require(builder.isValid()) { "moveCall must not be null" }
    return MoveCall(builder.moveCall!!)
  }

  /**
   * Creates a [TransferObjects] command.
   *
   * It requires that the [TransferObjectsBuilder.objects] and [TransferObjectsBuilder.to] are
   * provided otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun transferObjects(block: TransferObjectsBuilder.() -> Unit): Argument.Result {
    val builder = TransferObjectsBuilder().apply(block)
    require(builder.isValid()) { "objects and to must not be empty" }
    commands.add(TransferObjects(builder.objects, builder.to!!))
    return Argument.Result((commands.size - 1).toUShort())
  }

  /**
   * Creates a [SplitCoins] command.
   *
   * It requires that the [SplitCoinsBuilder.coin] and [SplitCoinsBuilder.into] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun splitCoins(block: SplitCoinsBuilder.() -> Unit): Argument.Result {
    val builder = SplitCoinsBuilder().apply(block)
    require(builder.isValid()) { "coin and into must not be empty" }
    commands.add(SplitCoins(builder.coin!!, builder.into))
    return Argument.Result((commands.size - 1).toUShort())
  }

  /**
   * Creates a [MergeCoins] command.
   *
   * It requires that the [MergeCoinsBuilder.coin] and [MergeCoinsBuilder.coins] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun mergeCoins(block: MergeCoinsBuilder.() -> Unit): Argument.Result {
    val builder = MergeCoinsBuilder().apply(block)
    require(builder.isValid()) { "coin and coins must not be empty" }
    commands.add(MergeCoins(builder.coin!!, builder.coins))
    return Argument.Result((commands.size - 1).toUShort())
  }

  /**
   * Creates a [Publish] command.
   *
   * It requires that the [PublishBuilder.bytes] and [PublishBuilder.dependencies] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun publish(block: PublishBuilder.() -> Unit): Argument.Result {
    val builder = PublishBuilder().apply(block)
    require(builder.isValid()) { "bytes and dependencies must not be empty" }
    commands.add(Publish(builder.bytes, builder.dependencies))
    return Argument.Result((commands.size - 1).toUShort())
  }

  /**
   * Creates a [MakeMoveVec] command.
   *
   * It requires that the [MakeMoveVecBuilder.typeTag] and [MakeMoveVecBuilder.values] are provided
   * otherwise it will throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun makeMoveVec(block: MakeMoveVecBuilder.() -> Unit): Argument.Result {
    val builder = MakeMoveVecBuilder().apply(block)
    require(builder.isValid()) { "typeTag and values must not be empty" }
    commands.add(MakeMoveVec(builder.typeTag, builder.values))
    return Argument.Result((commands.size - 1).toUShort())
  }

  /**
   * Creates a [Upgrade] command.
   *
   * It requires that the [UpgradeBuilder.modules], [UpgradeBuilder.dependencies],
   * [UpgradeBuilder.packageId], and [UpgradeBuilder.upgradeTicket] are provided otherwise it will
   * throw an [IllegalArgumentException].
   *
   * @param block to build the command.
   * @return The [Argument.Result] of the command.
   */
  fun upgrade(block: UpgradeBuilder.() -> Unit): Argument.Result {
    val builder = UpgradeBuilder().apply(block)
    require(builder.isValid()) {
      "modules, dependencies, packageId, and upgradeTicket must not be empty"
    }
    commands.add(
      Upgrade(builder.modules, builder.dependencies, builder.packageId, builder.upgradeTicket)
    )
    return Argument.Result((commands.size - 1).toUShort())
  }
}

interface Builder {
  fun isValid(): Boolean
}

class MoveCallBuilder : Builder {
  var moveCall: ProgrammableMoveCall? = null

  override fun isValid(): Boolean = moveCall != null
}

class TransferObjectsBuilder : Builder {
  lateinit var objects: List<Argument>
  var to: Argument? = null

  override fun isValid(): Boolean = to != null && objects.isNotEmpty()
}

class SplitCoinsBuilder : Builder {
  var coin: Argument? = null
  lateinit var into: List<Argument>

  override fun isValid(): Boolean = coin != null && into.isNotEmpty()
}

class MergeCoinsBuilder : Builder {
  var coin: Argument? = null
  lateinit var coins: List<Argument>

  override fun isValid(): Boolean = coin != null && coins.isNotEmpty()
}

class PublishBuilder : Builder {
  lateinit var bytes: List<List<Byte>>
  lateinit var dependencies: List<ObjectId>

  override fun isValid(): Boolean = dependencies.isNotEmpty() && bytes.isNotEmpty()
}

class MakeMoveVecBuilder : Builder {
  var typeTag: TypeTag? = null
  lateinit var values: List<Argument>

  override fun isValid(): Boolean = typeTag != null && values.isNotEmpty()
}

class UpgradeBuilder : Builder {
  lateinit var modules: List<List<Byte>>
  lateinit var dependencies: List<ObjectId>
  lateinit var packageId: ObjectId
  lateinit var upgradeTicket: Argument

  override fun isValid(): Boolean {
    TODO("Not yet implemented")
  }
}

// TODO: for module and function params, check on `Identifier` type
@Serializable
data class ProgrammableMoveCall(
  val pakage: ObjectId,
  val module: String,
  val function: String,
  val typeArguments: List<TypeTag>,
  val arguments: List<Argument>,
)

@Serializable(with = ArgumentSerializer::class)
sealed class Argument {

  @Serializable data object GasCoin : Argument()

  @Serializable data class Input(val index: UShort) : Argument()

  @Serializable data class Result(val commandResult: UShort) : Argument()

  @Serializable
  data class NestedResult(val commandIndex: UShort, val returnValueIndex: UShort) : Argument()
}
