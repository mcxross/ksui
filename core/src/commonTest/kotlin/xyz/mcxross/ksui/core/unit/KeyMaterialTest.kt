package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey
import xyz.mcxross.ksui.core.crypto.SECP256K1_ORDER
import xyz.mcxross.ksui.core.crypto.SECP256R1_ORDER
import xyz.mcxross.ksui.core.crypto.compareUnsigned
import xyz.mcxross.ksui.core.crypto.randomEcdsaSecretKey
import xyz.mcxross.ksui.core.model.Result

private val VALID_MNEMONIC = (List(11) { "abandon" } + "about").joinToString(" ")
private val BAD_CHECKSUM_MNEMONIC = List(12) { "abandon" }.joinToString(" ")
private val UNKNOWN_WORD_MNEMONIC = (List(11) { "abandon" } + "zzzzzz").joinToString(" ")

class KeyMaterialTest :
  StringSpec({
    "valid BIP-39 mnemonic imports" {
      Account.import(VALID_MNEMONIC).address shouldBe Account.import(VALID_MNEMONIC).address
    }

    "mnemonic with a bad checksum is rejected on every platform" {
      runCatching { Account.import(BAD_CHECKSUM_MNEMONIC) }
        .exceptionOrNull()
        .shouldBeInstanceOf<IllegalArgumentException>()
    }

    "mnemonic with a word outside the wordlist is rejected on every platform" {
      runCatching { Account.import(UNKNOWN_WORD_MNEMONIC) }
        .exceptionOrNull()
        .shouldBeInstanceOf<IllegalArgumentException>()
    }

    "generated Ed25519 keys are random and keep signing after repeated use" {
      val keys = List(4) { Ed25519PrivateKey() }
      keys.map { it.data.toList() }.toSet() shouldHaveSize 4
      val key = keys.first()
      repeat(3) {
        val message = byteArrayOf(it.toByte(), 1, 2, 3)
        val signature = (key.sign(message) as Result.Ok).value
        (key.publicKey.verify(message, signature) as Result.Ok).value shouldBe true
      }
    }

    "random ECDSA scalars are non-zero and below the curve order" {
      for (order in listOf(SECP256K1_ORDER, SECP256R1_ORDER)) {
        repeat(16) {
          val scalar = randomEcdsaSecretKey(order)
          scalar shouldHaveSize 32
          compareUnsigned(scalar, order) shouldBeLessThan 0
          scalar.any { it != 0.toByte() } shouldBe true
        }
      }
    }

    "compareUnsigned treats bytes as unsigned" {
      compareUnsigned(byteArrayOf(0x80.toByte()), byteArrayOf(0x7f)) shouldNotBe 0
      (compareUnsigned(byteArrayOf(0x80.toByte()), byteArrayOf(0x7f)) > 0) shouldBe true
      compareUnsigned(byteArrayOf(1, 2), byteArrayOf(1, 2)) shouldBe 0
    }
  })
