package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.PRIVATE_KEY_DATA
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.model.AccountAddress

class AccountAddressTest :
  StringSpec({
    "AccountAddress pads short hex literals" {
      val addr = AccountAddress.fromString("0x1")
      addr.toString() shouldBe "0x${"0".repeat(63)}1"
    }

    "AccountAddress accepts 64-hex strings without prefix" {
      val raw = "ab".repeat(32)
      val addr = AccountAddress.fromString(raw)
      addr.toString() shouldBe "0x$raw"
    }

    "AccountAddress derives from public key consistently" {
      val account = Account.import(PRIVATE_KEY_DATA)
      val derived = AccountAddress.fromPublicKey(account.publicKey)
      derived shouldBe account.address
    }

    "AccountAddress rejects invalid length without prefix" {
      val raw = "ab".repeat(31)
      val exception = runCatching { AccountAddress.fromString(raw) }.exceptionOrNull()
      exception.shouldBeInstanceOf<IllegalArgumentException>()
    }
  })
