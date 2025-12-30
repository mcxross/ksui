package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.model.Option
import xyz.mcxross.ksui.model.Result

class ResultOptionTest :
  StringSpec({
    "Result.expect returns value for Ok" {
      val result: Result<String, String> = Result.Ok("ok")
      result.expect("should not fail") shouldBe "ok"
    }

    "Result.expect throws for Err" {
      val result: Result<String, String> = Result.Err("boom")
      shouldThrow<IllegalStateException> { result.expect("failed") }
    }

    "Result.unwrapErr returns error for Err" {
      val result: Result<String, String> = Result.Err("boom")
      result.unwrapErr() shouldBe "boom"
    }

    "Option.expect returns value for Some" {
      val option: Option<String> = Option.Some("value")
      option.expect("missing") shouldBe "value"
    }

    "Option.expect throws for None" {
      val option: Option<String> = Option.None
      shouldThrow<NoSuchElementException> { option.expect("missing") }
    }
  })
