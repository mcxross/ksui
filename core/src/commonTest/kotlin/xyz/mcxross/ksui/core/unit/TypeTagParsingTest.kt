package xyz.mcxross.ksui.core.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.StructTag
import xyz.mcxross.ksui.core.model.TypeTag
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ptb
import xyz.mcxross.ksui.core.util.toTypeTag

private fun addr(short: String) = AccountAddress.fromString(short)

private val SUI = TypeTag.Struct(StructTag(addr("0x2"), "sui", "SUI"))

private fun coinOf(inner: TypeTag) = TypeTag.Struct(StructTag(addr("0x2"), "coin", "Coin", listOf(inner)))

class TypeTagParsingTest :
  StringSpec({
    "TypeTag parses vector types" {
      val tag = "vector<u64>".toTypeTag()
      tag.shouldBeInstanceOf<TypeTag.Vector>()
      tag.elementType shouldBe TypeTag.U64
    }

    "TypeTag parses struct types" {
      val tag = "0x2::sui::SUI".toTypeTag()
      val struct = tag.shouldBeInstanceOf<TypeTag.Struct>()
      struct.tag.address.toString() shouldBe "0x${"0".repeat(63)}2"
      struct.tag.module shouldBe "sui"
      struct.tag.name shouldBe "SUI"
    }

    "TypeTag parses every primitive" {
      mapOf(
          "bool" to TypeTag.Bool,
          "u8" to TypeTag.U8,
          "u16" to TypeTag.U16,
          "u32" to TypeTag.U32,
          "u64" to TypeTag.U64,
          "u128" to TypeTag.U128,
          "u256" to TypeTag.U256,
          "address" to TypeTag.Address,
          "signer" to TypeTag.Signer,
        )
        .forEach { (text, tag) -> text.toTypeTag() shouldBe tag }
    }

    "TypeTag keeps struct type parameters" {
      "0x2::coin::Coin<0x2::sui::SUI>".toTypeTag() shouldBe coinOf(SUI)
    }

    "TypeTag parses several and nested type parameters, with any spacing" {
      " 0x3::pool::Pool< 0x2::sui::SUI ,vector<u8>,  0x2::coin::Coin<0x2::sui::SUI> > ".toTypeTag() shouldBe
        TypeTag.Struct(StructTag(addr("0x3"), "pool", "Pool", listOf(SUI, TypeTag.Vector(TypeTag.U8), coinOf(SUI))))
    }

    "TypeTag parses vectors of generic structs" {
      "vector<vector<0x2::coin::Coin<0x2::sui::SUI>>>".toTypeTag() shouldBe
        TypeTag.Vector(TypeTag.Vector(coinOf(SUI)))
    }

    "TypeTag round-trips through toString" {
      listOf(
          "0x2::coin::Coin<0x2::sui::SUI>",
          "vector<0x2::coin::Coin<0x2::sui::SUI>>",
          "0x3::pool::Pool<0x2::sui::SUI, vector<u8>, 0x2::coin::Coin<0x2::sui::SUI>>",
          "0x2::dynamic_field::Field<u64, vector<address>>",
        )
        .forEach { text ->
          val tag = text.toTypeTag()
          tag.toString().toTypeTag() shouldBe tag
        }
    }

    "TypeTag accepts identifiers that Move accepts, including a leading underscore" {
      "0x2::_m::_T1".toTypeTag() shouldBe TypeTag.Struct(StructTag(addr("0x2"), "_m", "_T1"))
    }

    "TypeTag rejects incomplete or malformed types instead of dropping parts" {
      listOf(
          "not-a-type",
          "0x2::coin::Coin<0x2::sui::SUI", // unclosed type arguments
          "0x2::coin::Coin<u8>xyz", // trailing text
          "0x2::coin::Coin<>", // empty type arguments
          "0x2::m::T<u8,>", // trailing comma
          "0x2::coin", // missing struct name
          "0x2::::X",
          "0x2::m::1T",
          "0x2::_::T",
          "0xzz::m::T",
          "vector<u8",
          "vector<>",
          "u64 extra",
          "",
        )
        .forEach { text ->
          shouldThrow<IllegalArgumentException> { text.toTypeTag() }.message!! shouldContain "Could not parse TypeTag"
        }
    }

    "the PTB DSL's +\"…\" type arguments keep their type parameters" {
      val tx = ptb {
        moveCall {
          target = "0x2::coin::zero"
          typeArguments = +"0x2::coin::Coin<0x2::sui::SUI>"
        }
      }
      val call = tx.commands.single().shouldBeInstanceOf<Command.MoveCall>().moveCall
      call.typeArguments shouldBe listOf(coinOf(SUI))
    }
  })
