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
import xyz.mcxross.ksui.model.ObjectId
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.serializer.ArgumentSerializer
import xyz.mcxross.ksui.serializer.CommandSerializer

@Serializable(with = CommandSerializer::class)
open class Command {
  protected val commands: MutableList<Command> = mutableListOf()
  val list: List<Command>
    get() = commands

  @Serializable data class MoveCall(val moveCall: ProgrammableMoveCall) : Command()

  @Serializable
  data class TransferObjects(val objects: List<Argument>, val address: Argument) : Command()

  @Serializable data class SplitCoins(val coin: Argument, val into: List<Argument>) : Command()

  @Serializable data class MergeCoins(val coin: Argument, val coins: List<Argument>) : Command()

  @Serializable
  data class Publish(val bytes: List<List<Byte>>, val dependencies: List<ObjectId>) : Command()

  @Serializable
  data class MakeMoveVec(val typeTag: TypeTag?, val values: List<Argument>) : Command()

  @Serializable
  data class Upgrade(
    val modules: List<List<Byte>>,
    val dependencies: List<ObjectId>,
    val packageId: ObjectId,
    val upgradeTicket: Argument,
  ) : Command()
}

interface MoveCallScope {
  var target: String
  var typeArguments: List<TypeTag>
  var arguments: List<Argument>
}

interface TransferObjectsScope {
  var objects: List<Argument>
  var to: Argument?
}

interface SplitCoinsScope {
  var coin: Argument?
  var into: List<Argument>
}

interface MergeCoinsScope {
  var coin: Argument?
  var coins: List<Argument>
}

interface PublishScope {
  var bytes: List<List<Byte>>
  var dependencies: List<ObjectId>
}

interface MakeMoveVecScope {
  var typeTag: TypeTag?
  var values: List<Argument>
}

interface UpgradeScope {
  var modules: List<List<Byte>>
  var dependencies: List<ObjectId>
  var packageId: ObjectId
  var upgradeTicket: Argument
}

internal interface Builder {
  fun isValid(): Boolean
}

internal class MoveCallBuilder : MoveCallScope, Builder {
  override lateinit var target: String
  override var typeArguments: List<TypeTag> = emptyList()
  override var arguments: List<Argument> = emptyList()

  override fun isValid(): Boolean = this::target.isInitialized
}

internal class TransferObjectsBuilder : TransferObjectsScope, Builder {
  override lateinit var objects: List<Argument>
  override var to: Argument? = null

  override fun isValid(): Boolean = this::objects.isInitialized && to != null
}

internal class SplitCoinsBuilder : SplitCoinsScope, Builder {
  override var coin: Argument? = null
  override lateinit var into: List<Argument>

  override fun isValid(): Boolean = coin != null && this::into.isInitialized
}

internal class MergeCoinsBuilder : MergeCoinsScope, Builder {
  override var coin: Argument? = null
  override lateinit var coins: List<Argument>

  override fun isValid(): Boolean = coin != null && this::coins.isInitialized
}

internal class PublishBuilder : PublishScope, Builder {
  override lateinit var bytes: List<List<Byte>>
  override lateinit var dependencies: List<ObjectId>

  override fun isValid(): Boolean = this::bytes.isInitialized && this::dependencies.isInitialized
}

internal class MakeMoveVecBuilder : MakeMoveVecScope, Builder {
  override var typeTag: TypeTag? = null
  override lateinit var values: List<Argument>

  override fun isValid(): Boolean = typeTag != null && this::values.isInitialized
}

internal class UpgradeBuilder : UpgradeScope, Builder {
  override lateinit var modules: List<List<Byte>>
  override lateinit var dependencies: List<ObjectId>
  override lateinit var packageId: ObjectId
  override lateinit var upgradeTicket: Argument

  override fun isValid(): Boolean =
    this::modules.isInitialized &&
      this::dependencies.isInitialized &&
      this::packageId.isInitialized &&
      this::upgradeTicket.isInitialized
}

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
