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
package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.Command
import xyz.mcxross.ksui.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.ptb.PtbDsl
import xyz.mcxross.ksui.util.toTypeTag

const val HELLO_WORLD =
  "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d::hello_wolrd::hello_world"

const val VALID_ADDR = "0x0000000000000000000000000000000000000000000000000000000000000123"

class CommandTest :
  StringSpec({
    fun testPtbConstruction(block: PtbDsl.() -> Unit): ProgrammableTransactionBuilder {
      val builder = ProgrammableTransactionBuilder()
      val dsl = PtbDsl(builder)
      dsl.block()
      return builder
    }

    "should generate a single command from moveCall" {
      val builder = testPtbConstruction { moveCall { target = HELLO_WORLD } }

      builder.list shouldHaveSize 1
    }

    "should parse moveCall package, module, and function correctly" {
      val builder = testPtbConstruction { moveCall { target = HELLO_WORLD } }

      val command = builder.list.first()
      command.shouldBeInstanceOf<Command.MoveCall>()

      val moveCall = command.moveCall

      moveCall.pakage.toString() shouldBe
        "0x883393ee444fb828aa0e977670cf233b0078b41d144e6208719557cb3888244d"

      moveCall.module shouldBe "hello_wolrd"
      moveCall.function shouldBe "hello_world"
      moveCall.arguments shouldHaveSize 0
    }

    "should handle moveCall with multiple type arguments" {
      val builder = testPtbConstruction {
        moveCall {
          target = "0x2::coin::withdraw"
          typeArguments = listOf(TypeTag.U64, "0x2::sui::SUI".toTypeTag())
          arguments = listOf(pure(100UL))
        }
      }

      builder.list shouldHaveSize 1
      val command = builder.list.first()
      command.shouldBeInstanceOf<Command.MoveCall>()

      val moveCall = command.moveCall
      moveCall.typeArguments shouldHaveSize 2

      moveCall.typeArguments[0] shouldBe TypeTag.U64

      val secondArg = moveCall.typeArguments[1]
      secondArg.shouldBeInstanceOf<TypeTag.Struct>()

      val structTag = secondArg.tag
      structTag.address.toString() shouldBe
        "0x0000000000000000000000000000000000000000000000000000000000000002"
      structTag.module shouldBe "sui"
      structTag.name shouldBe "SUI"
    }

    "should correctly map arguments in transferObjects" {
      val builder = testPtbConstruction {
        val obj1 = pure(100UL)
        val obj2 = pure(200UL)
        val recipient = address(VALID_ADDR)

        transferObjects {
          objects = listOf(obj1, obj2)
          to = recipient
        }
      }

      builder.list shouldHaveSize 1
      val command = builder.list.first()
      command.shouldBeInstanceOf<Command.TransferObjects>()

      command.objects shouldHaveSize 2
      command.objects[0] shouldBe Argument.Input(0u)
      command.objects[1] shouldBe Argument.Input(1u)
      command.address shouldBe Argument.Input(2u)
    }

    "should correctly construct splitCoins with GasCoin" {
      val builder = testPtbConstruction {
        splitCoins {
          coin = Argument.GasCoin
          into = listOf(pure(1_000_000UL), pure(2_000_000UL), pure(3_000_000UL))
        }
      }

      builder.list shouldHaveSize 1
      val command = builder.list.first()
      command.shouldBeInstanceOf<Command.SplitCoins>()

      command.coin shouldBe Argument.GasCoin
      command.into shouldHaveSize 3
      command.into[0] shouldBe Argument.Input(0u)
    }

    "should construct mergeCoins with correct input indices" {
      val builder = testPtbConstruction {
        val destCoin = pure(100UL)
        val srcCoin1 = pure(10UL)
        val srcCoin2 = pure(20UL)

        mergeCoins {
          coin = destCoin
          coins = listOf(srcCoin1, srcCoin2)
        }
      }

      builder.list shouldHaveSize 1
      val command = builder.list.first()
      command.shouldBeInstanceOf<Command.MergeCoins>()

      command.coin shouldBe Argument.Input(0u)
      command.coins shouldHaveSize 2
      command.coins[0] shouldBe Argument.Input(1u)
    }

    "should handle makeMoveVec with type tags" {
      val builder = testPtbConstruction {
        val item1 = pure(1.toByte())
        val item2 = pure(2.toByte())

        makeMoveVec {
          typeTag = TypeTag.U8
          values = listOf(item1, item2)
        }
      }

      builder.list shouldHaveSize 1
      val command = builder.list.first()
      command.shouldBeInstanceOf<Command.MakeMoveVec>()

      command.typeTag shouldBe TypeTag.U8
      command.values shouldHaveSize 2
    }

    "should chain results from one command to another (NestedResult)" {
      val builder = testPtbConstruction {
        val newCoins = splitCoins {
          coin = Argument.GasCoin
          into = listOf(pure(500UL))
        }

        transferObjects {
          objects = listOf(newCoins[0])
          to = address(VALID_ADDR)
        }
      }

      builder.list shouldHaveSize 2
      val transferCmd = builder.list[1]
      transferCmd.shouldBeInstanceOf<Command.TransferObjects>()

      val objectArg = transferCmd.objects[0]
      objectArg.shouldBeInstanceOf<Argument.NestedResult>()
      objectArg.commandIndex shouldBe 0u
      objectArg.returnValueIndex shouldBe 0u
    }
  })
