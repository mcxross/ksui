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
package xyz.mcxross.ksui.unit

import kotlin.test.Test
import kotlin.test.assertTrue
import xyz.mcxross.ksui.PRIVATE_KEY_DATA
import xyz.mcxross.ksui.account.Account
import xyz.mcxross.ksui.account.Ed25519Account
import xyz.mcxross.ksui.core.crypto.Ed25519PublicKey
import xyz.mcxross.ksui.core.crypto.PrivateKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme

class AccountTest {

  // Test account generation
  @Test
  fun testAccountGeneration() {
    val account = Account.create()
    assertTrue(
      "Account generation failed. Default Account signature scheme must be SignatureScheme.ED25519"
    ) {
      account.scheme == SignatureScheme.ED25519
    }
    assertTrue("Account generation failed. Default public key must be of type Ed25519PublicKey") {
      account.publicKey is Ed25519PublicKey
    }
    assertTrue("Account generation failed. Mnemonic can't be empty.") {
      (account as Ed25519Account).mnemonic.isNotEmpty()
    }
    assertTrue("Account generation failed. Default word length expected to be 12 words.") {
      (account as Ed25519Account).mnemonic.split(" ").size == 12
    }
    assertTrue("Account generation failed. Address can't be empty.") {
      (account as Ed25519Account).address.toString().isNotEmpty()
    }
    assertTrue("Account generation failed. Invalid address length.") {
      account.address.toString().length == 66
    }
  }

  @Test
  fun testAccountImportPhrase() {
    val account =
      Account.import("dry clock defense build educate lonely cycle hand phrase kitchen enemy seed")
    assertTrue(
      "Account import failed. Default Account signature scheme must be SignatureScheme.ED25519"
    ) {
      account.scheme == SignatureScheme.ED25519
    }
    assertTrue("Account import failed. Default public key must be of type Ed25519PublicKey") {
      account.publicKey is Ed25519PublicKey
    }
    assertTrue("Account import failed. Mnemonic can't be empty.") {
      (account as Ed25519Account).mnemonic.isNotEmpty()
    }
    assertTrue("Account import failed. Default word length expected to be 12 words.") {
      (account as Ed25519Account).mnemonic.split(" ").size == 12
    }
    assertTrue("Account import failed. Address can't be empty.") {
      account.address.toString().isNotEmpty()
    }
    assertTrue("Account import failed. Invalid address length.") {
      account.address.toString().length == 66
    }
  }

  @Test
  fun testAccountImportPrivateKeyString() {
    val account = Account.import(PRIVATE_KEY_DATA)
    assertTrue {
      account.address.toString() ==
        "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a904"
    }
  }

  @Test
  fun testAccountImportPrivateKeyInstance() {
    val account = Account.import(PrivateKey.fromEncoded(PRIVATE_KEY_DATA))
    assertTrue {
      account.address.toString() ==
        "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a904"
    }
  }
}
