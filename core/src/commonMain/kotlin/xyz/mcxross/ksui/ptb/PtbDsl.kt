/*
 * Copyright 2025 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.ptb

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.util.toTypeTag

/**
 * A dedicated DSL receiver for the `ptb { ... }` block, providing a context-specific API. This
 * class contains only the lambda-style command functions.
 */
class PtbDsl(val builder: ProgrammableTransactionBuilder) {

  fun pure(bytes: ByteArray): Argument = builder.pure(bytes)

  inline fun <reified T> pure(value: T): Argument = builder.pure(value)

  fun `object`(id: String): Argument = builder.`object`(id)

  fun `object`(objectArg: ObjectArg): Argument = builder.`object`(objectArg)

  fun address(str: String): Argument = builder.address(str)

  fun address(account: xyz.mcxross.ksui.account.Account): Argument = builder.address(account)

  fun address(address: AccountAddress): Argument = builder.address(address)

  fun system(): Argument = builder.system()

  fun clock(): Argument = builder.clock()

  fun random(): Argument = builder.random()

  fun denyList(): Argument = builder.denyList()

  fun arg(value: Boolean): Argument = builder.arg(value)

  fun arg(value: UByte): Argument = builder.arg(value)

  fun arg(value: UShort): Argument = builder.arg(value)

  fun arg(value: UInt): Argument = builder.arg(value)

  fun arg(value: ULong): Argument = builder.arg(value)

  fun arg(value: String): Argument = builder.arg(value)

  fun arg(value: AccountAddress): Argument = builder.arg(value)

  fun <T> arg(values: List<T>, elementType: TypeTag): Argument = builder.arg(values, elementType)

  fun moveCall(block: MoveCallScope.() -> Unit): Argument.Result {
    val b = MoveCallBuilder().apply(block)
    require(b.isValid()) { "MoveCallBuilder is not valid" }
    val parts = b.target.split("::")
    require(parts.size == 3) { "target must be in the format of `package::module::function`" }
    return builder.moveCall(target = b.target, b.typeArguments, b.arguments)
  }

  fun splitCoins(block: SplitCoinsScope.() -> Unit): List<Argument.NestedResult> {
    val b = SplitCoinsBuilder().apply(block)
    require(b.isValid()) { "SplitCoinsBuilder is not valid" }
    return builder.splitCoins(b.coin!!, b.into)
  }

  fun transferObjects(block: TransferObjectsScope.() -> Unit): Argument.Result {
    val b = TransferObjectsBuilder().apply(block)
    require(b.isValid()) { "TransferObjectsBuilder is not valid" }
    return builder.transferObjects(b.objects, b.to!!)
  }

  fun mergeCoins(block: MergeCoinsScope.() -> Unit): Argument.Result {
    val b = MergeCoinsBuilder().apply(block)
    require(b.isValid()) { "MergeCoinsBuilder is not valid" }
    return builder.mergeCoins(b.coin!!, b.coins)
  }

  fun publish(block: PublishScope.() -> Unit): Argument.Result {
    val b = PublishBuilder().apply(block)
    require(b.isValid()) { "PublishBuilder is not valid" }
    return builder.publish(b.bytes, b.dependencies)
  }

  fun makeMoveVec(block: MakeMoveVecScope.() -> Unit): Argument.Result {
    val b = MakeMoveVecBuilder().apply(block)
    require(b.isValid()) { "MakeMoveVecBuilder is not valid" }
    return builder.makeMoveVec(b.typeTag, b.values)
  }

  fun upgrade(block: UpgradeScope.() -> Unit): Argument.Result {
    val b = UpgradeBuilder().apply(block)
    require(b.isValid()) { "UpgradeBuilder is not valid" }
    return builder.upgrade(b.modules, b.dependencies, b.packageId, b.upgradeTicket)
  }

  /**
   * Defines the unary plus operator for a single `Argument`.
   *
   * This allows you to start an argument list with a single argument using the `+` prefix, which is
   * a highly idiomatic way to begin a collection in a DSL.
   *
   * @return A new `List<Argument>` containing just this single argument.
   */
  operator fun Argument.unaryPlus(): List<Argument> {
    return listOf(this)
  }

  /**
   * Defines the `+` operator for combining two `Argument` objects into a `List<Argument>`.
   *
   * This is the starting point for creating a fluent, chainable syntax for building argument lists
   * for a `moveCall`. Once a list is created, Kotlin's standard library `plus` operator can be used
   * to append additional arguments.
   *
   * @param other The `Argument` to add to the right-hand side.
   * @return A new `List<Argument>` containing both arguments.
   */
  operator fun Argument.plus(other: Argument): List<Argument> {
    return listOf(this, other)
  }

  operator fun TypeTag.unaryPlus(): List<TypeTag> = listOf(this)

  operator fun TypeTag.plus(other: TypeTag): List<TypeTag> = listOf(this, other)

  operator fun String.unaryPlus(): List<TypeTag> = listOf(this.toTypeTag())
}
