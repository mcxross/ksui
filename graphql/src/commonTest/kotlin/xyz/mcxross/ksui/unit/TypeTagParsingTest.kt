package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.model.TypeTag
import xyz.mcxross.ksui.util.toTypeTag

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

    "TypeTag rejects unknown formats" {
      shouldThrow<IllegalArgumentException> { "not-a-type".toTypeTag() }
    }
  })
