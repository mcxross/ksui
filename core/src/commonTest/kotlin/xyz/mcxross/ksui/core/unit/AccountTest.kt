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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.PRIVATE_KEY_DATA
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.account.Ed25519Account
import xyz.mcxross.ksui.core.account.Secp256k1Account
import xyz.mcxross.ksui.core.account.Secp256r1Account
import xyz.mcxross.ksui.core.crypto.Ed25519PublicKey
import xyz.mcxross.ksui.core.crypto.PrivateKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme

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

    "Account import from phrase uses default scheme and keys" {
      val account =
        Account.import(
          "dry clock defense build educate lonely cycle hand phrase kitchen enemy seed"
        )

      account.scheme shouldBe SignatureScheme.ED25519
      account.publicKey.shouldBeInstanceOf<Ed25519PublicKey>()

      val edAccount = account.shouldBeInstanceOf<Ed25519Account>()
      edAccount.mnemonic.isNotEmpty() shouldBe true
      edAccount.mnemonic.split(" ").size shouldBe 12
      edAccount.address.toString().isNotEmpty() shouldBe true
      account.address.toString().length shouldBe 66
    }

    "Account import from private key string resolves correct address" {
      val account = Account.import(PRIVATE_KEY_DATA)
      account.address.toString().length shouldBe 66
    }

    "Account import from private key instance resolves correct address" {
      val account = Account.import(PrivateKey.fromEncoded(PRIVATE_KEY_DATA))
      account.address shouldBe Account.import(PRIVATE_KEY_DATA).address
    }

    "Account string representations never expose recovery material" {
      val accounts =
        listOf(
          (Account.create(SignatureScheme.ED25519) as Ed25519Account).let { it to it.mnemonic },
          (Account.create(SignatureScheme.Secp256k1) as Secp256k1Account).let {
            it to it.mnemonic
          },
          (Account.create(SignatureScheme.Secp256r1) as Secp256r1Account).let {
            it to it.mnemonic
          },
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
