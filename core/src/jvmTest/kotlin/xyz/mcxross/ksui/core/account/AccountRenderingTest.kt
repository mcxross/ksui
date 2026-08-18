package xyz.mcxross.ksui.core.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.core.crypto.SignatureScheme

class AccountRenderingTest :
  StringSpec({
    "Secp256k1 account string representation never exposes recovery material" {
      assertDoesNotExposeRecoveryMaterial(Account.create(SignatureScheme.Secp256k1))
    }

    "Secp256r1 account string representation never exposes recovery material" {
      assertDoesNotExposeRecoveryMaterial(Account.create(SignatureScheme.Secp256r1))
    }
  })

private fun assertDoesNotExposeRecoveryMaterial(account: Account) {
  val mnemonic =
    when (account) {
      is Secp256k1Account -> account.mnemonic
      is Secp256r1Account -> account.mnemonic
      else -> error("Expected a supported Secp256 account")
    }
  val rendered = account.toString()

  rendered.contains(mnemonic) shouldBe false
  rendered.contains("mnemonic=") shouldBe false
  rendered.contains("privKey=") shouldBe false
  rendered.contains(account.address.toString()) shouldBe true
}
