package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectId
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableMoveCall
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction

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

    "Command.MakeMoveVec encodes type as BCS Option<TypeTag>" {
      val original: Command =
        Command.MakeMoveVec(
          typeTag = TypeTag.U8,
          values = listOf(Argument.Input(0u), Argument.Input(1u)),
        )

      val bytes = bcs.encodeToByteArray<Command>(original)

      Hex(bytes).toStringWithoutPrefix() shouldBe "05010102010000010100"
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
  })
