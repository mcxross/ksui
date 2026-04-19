package xyz.mcxross.ksui.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.core.crypto.Ed25519PublicKey
import xyz.mcxross.ksui.core.crypto.PasskeyPublicKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PublicKey
import xyz.mcxross.ksui.model.AccountAddress

class AccountAddressPublicKeyTest :
  StringSpec({
    "AccountAddress.fromPublicKey accepts valid Ed25519 length" {
      val pubkey = Ed25519PublicKey(ByteArray(32))
      val address = AccountAddress.fromPublicKey(pubkey)
      address.toString().length shouldBe 66
    }

    "AccountAddress.fromPublicKey rejects invalid Ed25519 length" {
      shouldThrow<IllegalArgumentException> {
        AccountAddress.fromPublicKey(Ed25519PublicKey(ByteArray(31)))
      }
    }

    "AccountAddress.fromPublicKey rejects invalid Secp256k1 length" {
      shouldThrow<IllegalArgumentException> {
        AccountAddress.fromPublicKey(Secp256k1PublicKey(ByteArray(32)))
      }
    }

    "AccountAddress.fromPublicKey rejects invalid Passkey length" {
      shouldThrow<IllegalArgumentException> {
        AccountAddress.fromPublicKey(PasskeyPublicKey(ByteArray(32)))
      }
    }
  })
