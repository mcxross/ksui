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
import org.komputing.kbech32.Bech32
import xyz.mcxross.ksui.core.PRIVATE_KEY_DATA
import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey
import xyz.mcxross.ksui.core.crypto.PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.util.convertBits

class PrivateKeyTest :
  StringSpec({
    "Private key generation uses 32-byte seed" {
      val privateKey = Ed25519PrivateKey()
      privateKey.data.size shouldBe 32
    }

    "Private key import via constructor preserves bytes" {
      val privateKey = Ed25519PrivateKey(PRIVATE_KEY_DATA)
      privateKey.data.size shouldBe 32
      privateKey.export() shouldBe PRIVATE_KEY_DATA
    }

    "Private key import via encoded bytes preserves bytes" {
      val privateKey = PrivateKey.fromEncoded(PRIVATE_KEY_DATA)
      privateKey.data.size shouldBe 32
      privateKey.export() shouldBe PRIVATE_KEY_DATA
    }

    "PrivateKey.fromEncoded rejects wrong HRP and invalid lengths" {
      val wrongHrp =
        Bech32.encode("cosmos", convertBits(ByteArray(33) { 1 }, 8, 5, true))
      shouldThrow<IllegalArgumentException> { PrivateKey.fromEncoded(wrongHrp) }

      val wrongLen =
        Bech32.encode("suiprivkey", convertBits(ByteArray(17) { 0 }, 8, 5, true))
      shouldThrow<IllegalArgumentException> { PrivateKey.fromEncoded(wrongLen) }
    }

    "Typed private key constructor rejects cross-scheme Bech32 strings" {
      val k1Encoded = Secp256k1PrivateKey(ByteArray(32) { 3 }).export()
      shouldThrow<IllegalArgumentException> { Ed25519PrivateKey(k1Encoded) }
    }

    "Mutating returned PrivateKey.data does not mutate stored key" {
      val privateKey = Ed25519PrivateKey(PRIVATE_KEY_DATA)
      val leaked = privateKey.data
      leaked.fill(0)
      privateKey.export() shouldBe PRIVATE_KEY_DATA
    }
  })
