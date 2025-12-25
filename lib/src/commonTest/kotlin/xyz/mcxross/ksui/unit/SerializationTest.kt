package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.CallArg
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectId
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.ptb.Argument
import xyz.mcxross.ksui.ptb.Command
import xyz.mcxross.ksui.ptb.ProgrammableMoveCall
import xyz.mcxross.ksui.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.randomString

class SerializationTest :
  StringSpec({
    val bcs = Bcs

    // ========================================================================================
    // 1. OBJECT ARGUMENT TESTS (Inner Layer)
    // ========================================================================================

    "RoundTrip: ObjectArg.ImmOrOwnedObject" {
      val original =
        ObjectArg.ImmOrOwnedObject(
          ObjectReference(
            Reference(AccountAddress.fromString("0x123")),
            100L,
            ObjectDigest(Digest("BqrFumAb7yjZKKQUApKjzqdgHUZY3XaTNWxNQsvjMSBc")),
          )
        )

      val bytes = bcs.encodeToByteArray<ObjectArg>(original)
      val deserialized = bcs.decodeFromByteArray<ObjectArg>(bytes)

      deserialized.shouldBeInstanceOf<ObjectArg.ImmOrOwnedObject>()
      deserialized.objectRef.digest.digest.data.contentEquals(original.objectRef.digest.digest.data)
      deserialized.objectRef.version shouldBe 100L
    }

    "RoundTrip: ObjectArg.SharedObject" {
      val original =
        ObjectArg.SharedObject(ObjectId(AccountAddress.fromString("0x123")), 123L, true)

      val string = bcs.encodeToByteArray<ObjectArg>(original)
      val deserialized = bcs.decodeFromByteArray<ObjectArg>(string)

      deserialized.shouldBeInstanceOf<ObjectArg.SharedObject>()
      deserialized.id.toString() shouldBe original.id.toString()
      deserialized.mutable shouldBe true
    }

    "RoundTrip: ObjectArg.Receiving" {
      val original =
        ObjectArg.Receiving(
          ObjectReference(
            Reference(AccountAddress.fromString("0x123")),
            55L,
            ObjectDigest(Digest("BqrFumAb7yjZKKQUApKjzqdgHUZY3XaTNWxNQsvjMSBc")),
          )
        )

      val string = bcs.encodeToByteArray<ObjectArg>(original)
      val deserialized = bcs.decodeFromByteArray<ObjectArg>(string)

      deserialized.shouldBeInstanceOf<ObjectArg.Receiving>()
    }

    "RoundTrip: CallArg.Pure" {
      val data = byteArrayOf(1, 2, 3, 4, 5)
      val original = CallArg.Pure(data)

      val bytes = bcs.encodeToByteArray<CallArg>(original)
      val deserialized = bcs.decodeFromByteArray<CallArg>(bytes)

      deserialized.shouldBeInstanceOf<CallArg.Pure>()
      deserialized.data.toList() shouldBe data.toList()
    }

    "RoundTrip: CallArg.Object" {
      val inner = ObjectArg.SharedObject(ObjectId(AccountAddress.fromString("0x1")), 1, false)
      val original = CallArg.Object(inner)

      val bytes = bcs.encodeToByteArray<CallArg>(original)
      val deserialized = bcs.decodeFromByteArray<CallArg>(bytes)

      deserialized.shouldBeInstanceOf<CallArg.Object>()
      deserialized.arg.shouldBeInstanceOf<ObjectArg.SharedObject>()
    }

    // ========================================================================================
    // 3. COMMAND TESTS (Logic Layer)
    // ========================================================================================

    "RoundTrip: Command.MoveCall" {
      val original =
        Command.MoveCall(
          ProgrammableMoveCall(
            ObjectId(AccountAddress.fromString("0xff")),
            "my_module",
            "my_function",
            listOf(TypeTag.U64),
            listOf(Argument.Input(0u), Argument.GasCoin),
          )
        )

      val string = bcs.encodeToByteArray<Command.MoveCall>(original)
      val deserialized = bcs.decodeFromByteArray<Command.MoveCall>(string)

      deserialized.shouldBeInstanceOf<Command.MoveCall>()
      val call = deserialized.moveCall

      call.module shouldBe "my_module"
      call.arguments.size shouldBe 2
      call.arguments[1] shouldBe Argument.GasCoin
    }

    "RoundTrip: Command.TransferObjects" {
      val original =
        Command.TransferObjects(
          objects = listOf(Argument.Input(0u), Argument.NestedResult(1u, 2u)),
          address = Argument.Input(1u),
        )

      val bytes = bcs.encodeToByteArray(original)
      val deserialized = bcs.decodeFromByteArray<Command.TransferObjects>(bytes)

      deserialized.shouldBeInstanceOf<Command.TransferObjects>()

      deserialized.objects.size shouldBe 2
      deserialized.objects[1].shouldBeInstanceOf<Argument.NestedResult>()
    }

    "RoundTrip: Command.SplitCoins" {
      val original =
        Command.SplitCoins(
          coin = Argument.GasCoin,
          into = listOf(Argument.Input(0u), Argument.Input(1u)),
        )

      val bytes = bcs.encodeToByteArray(original)
      val deserialized = bcs.decodeFromByteArray<Command.SplitCoins>(bytes)

      deserialized.shouldBeInstanceOf<Command.SplitCoins>()

      deserialized.coin shouldBe Argument.GasCoin
      deserialized.into.size shouldBe 2
    }

    "RoundTrip: Command.MergeCoins" {
      val original =
        Command.MergeCoins(
          coin = Argument.Input(0u),
          coins = listOf(Argument.Input(1u), Argument.Input(2u)),
        )

      val bytes = bcs.encodeToByteArray(original)
      val deserialized = bcs.decodeFromByteArray<Command.MergeCoins>(bytes)

      deserialized.shouldBeInstanceOf<Command.MergeCoins>()
      deserialized.coins.size shouldBe 2
    }

    "RoundTrip: Command.MakeMoveVec" {
      val original = Command.MakeMoveVec(typeTag = TypeTag.U8, values = listOf(Argument.Input(0u)))

      val string = bcs.encodeToByteArray(original)
      val deserialized = bcs.decodeFromByteArray<Command.MakeMoveVec>(string)

      deserialized.shouldBeInstanceOf<Command.MakeMoveVec>()
      deserialized.typeTag shouldBe TypeTag.U8
    }

    // ========================================================================================
    // 4. PROGRAMMABLE TRANSACTION TESTS (Master Layer)
    // ========================================================================================

    "RoundTrip: Full ProgrammableTransaction" {
      val inputs =
        listOf(
          CallArg.Pure(byteArrayOf(100)),
          CallArg.Object(
            ObjectArg.SharedObject(ObjectId(AccountAddress.fromString("0x123")), 1, true)
          ),
        )

      val commands =
        listOf(
          Command.SplitCoins(Argument.GasCoin, listOf(Argument.Input(0u))),
          Command.TransferObjects(listOf(Argument.NestedResult(0u, 0u)), Argument.Input(1u)),
        )

      val original = ProgrammableTransaction(inputs, commands)

      val bytes = bcs.encodeToByteArray<ProgrammableTransaction>(original)
      val deserialized = bcs.decodeFromByteArray<ProgrammableTransaction>(bytes)

      deserialized.inputs.size shouldBe 2
      deserialized.commands.size shouldBe 2

      deserialized.inputs[0].shouldBeInstanceOf<CallArg.Pure>()
      deserialized.commands[0].shouldBeInstanceOf<Command.SplitCoins>()

      val split = deserialized.commands[0] as Command.SplitCoins
      split.coin shouldBe Argument.GasCoin
    }

    "RoundTrip: DSL Complex Construction" {
      val sui = sui
      val ptb = ptb {
        val ramp = `object`("0x67a60352909987c0a3777d15444e883599ee8799c742aa97b6a23205da29867a")

        val auth = moveCall {
          target =
            "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::authenticate"
          arguments = listOf(ramp)
        }

        val params = moveCall {
          target =
            "0x10c87c29ea5d5674458652ababa246742a763f9deafed11608b7f0baea296484::intents::new_params"
          arguments =
            listOf(
              pure(randomString(10)),
              pure("description"),
              pure(listOf(0UL)),
              pure(900000UL),
              `object`("0x6"),
            )
        }

        val outcome = moveCall {
          target =
            "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::empty_approved_outcome"
        }

        val members =
          pure(
            listOf(
              "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a904",
              "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a902",
            )
          )

        moveCall {
          target =
            "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::config::request_config_p2p_ramp"
          arguments = listOf(auth, params, outcome, ramp, members)
        }
      }

      val bytes = bcs.encodeToByteArray<ProgrammableTransaction>(ptb)
      val deserialized = bcs.decodeFromByteArray<ProgrammableTransaction>(bytes)

      deserialized.inputs.size shouldBe 7
      deserialized.inputs[0].shouldBeInstanceOf<CallArg.Object>()
      deserialized.inputs[1].shouldBeInstanceOf<CallArg.Pure>()

      deserialized.commands.size shouldBe 4

      val cmd0 = deserialized.commands[0]
      cmd0.shouldBeInstanceOf<Command.MoveCall>()
      cmd0.moveCall.function shouldBe "authenticate"
      cmd0.moveCall.arguments[0] shouldBe Argument.Input(0u)

      val cmd4 = deserialized.commands[3]
      cmd4.shouldBeInstanceOf<Command.MoveCall>()
      cmd4.moveCall.function shouldBe "request_config_p2p_ramp"

      val args = cmd4.moveCall.arguments
      args.size shouldBe 5

      args[0].shouldBeInstanceOf<Argument.Result>()
      (args[0] as Argument.Result).commandResult shouldBe 0u

      args[1].shouldBeInstanceOf<Argument.Result>()
      (args[1] as Argument.Result).commandResult shouldBe 1u

      args[2].shouldBeInstanceOf<Argument.Result>()
      (args[2] as Argument.Result).commandResult shouldBe 2u

      args[3] shouldBe Argument.Input(0u)

      args[4] shouldBe Argument.Input(6u)
    }
  })
