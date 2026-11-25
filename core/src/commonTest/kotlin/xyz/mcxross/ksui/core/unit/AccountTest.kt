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
package xyz.mcxross.ksui.core.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.PRIVATE_KEY_DATA
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.account.Ed25519Account
import xyz.mcxross.ksui.core.account.Secp256k1Account
import xyz.mcxross.ksui.core.account.Secp256r1Account
import xyz.mcxross.ksui.core.crypto.Ed25519PublicKey
import xyz.mcxross.ksui.core.crypto.PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result

class AccountTest :
  StringSpec({
    "Account generation uses default scheme, keys, and mnemonic" {
      val account = Account.create()

      account.scheme shouldBe SignatureScheme.ED25519
      account.publicKey.shouldBeInstanceOf<Ed25519PublicKey>()

      val edAccount = account.shouldBeInstanceOf<Ed25519Account>()
      edAccount.mnemonic.isNotEmpty() shouldBe true
      edAccount.mnemonic.split(" ").size shouldBe 12
      edAccount.address.toString().isNotEmpty() shouldBe true
      account.address.toString().length shouldBe 66
    }

    "Account import from phrase supports Ed25519, Secp256k1, and Secp256r1" {
      val phrase = "dry clock defense build educate lonely cycle hand phrase kitchen enemy seed"
      val edAccount = Account.import(phrase).shouldBeInstanceOf<Ed25519Account>()
      val k1Account =
        Account.import(phrase, SignatureScheme.Secp256k1).shouldBeInstanceOf<Secp256k1Account>()
      val r1Account =
        Account.import(phrase, SignatureScheme.Secp256r1).shouldBeInstanceOf<Secp256r1Account>()

      edAccount.scheme shouldBe SignatureScheme.ED25519
      k1Account.scheme shouldBe SignatureScheme.Secp256k1
      r1Account.scheme shouldBe SignatureScheme.Secp256r1
      edAccount.address shouldNotBe k1Account.address
      k1Account.address shouldNotBe r1Account.address
    }

    "Ed25519 derivation respects custom hardened account path" {
      val words =
        "dry clock defense build educate lonely cycle hand phrase kitchen enemy seed".split(" ")
      val key0 = derivePrivateKeyFromMnemonic(words, SignatureScheme.ED25519, "m/44'/784'/0'/0'/0'")
      val key1 = derivePrivateKeyFromMnemonic(words, SignatureScheme.ED25519, "m/44'/784'/0'/0'/1'")

      key0.contentEquals(key1) shouldBe false
    }

    "Account import from private key string resolves correct address and enforces explicit scheme" {
      val account = Account.import(PRIVATE_KEY_DATA)
      account.address.toString().length shouldBe 66

      val k1Encoded = Secp256k1PrivateKey(ByteArray(32) { (it + 2).toByte() }).export()
      Account.import(k1Encoded).scheme shouldBe SignatureScheme.Secp256k1
      shouldThrow<IllegalArgumentException> {
        Account.import(k1Encoded, SignatureScheme.ED25519)
      }
    }

    "Account import from private key instance resolves correct address" {
      val account = Account.import(PrivateKey.fromEncoded(PRIVATE_KEY_DATA))
      account.address shouldBe Account.import(PRIVATE_KEY_DATA).address
    }

    "Account close zeroizes key material and prevents further signing" {
      val account = Account.create()
      account.close()
      account.sign(byteArrayOf(1, 2, 3)).shouldBeInstanceOf<Result.Err<*>>()
    }

    "AccountAddress.EMPTY returns isolated byte arrays" {
      AccountAddress.EMPTY.data[0] = 0x7f
      AccountAddress.EMPTY.data[0] shouldBe 0
    }

    "Account string representations never expose recovery material" {
      val accounts =
        listOf(
          (Account.create(SignatureScheme.ED25519) as Ed25519Account).let { it to it.mnemonic },
        )

      accounts.forEach { (account, mnemonic) ->
        val rendered = account.toString()
        rendered.contains(mnemonic) shouldBe false
        rendered.contains("mnemonic=") shouldBe false
        rendered.contains("privKey=") shouldBe false
        rendered.contains(account.address.toString()) shouldBe true
      }
    }
  })
