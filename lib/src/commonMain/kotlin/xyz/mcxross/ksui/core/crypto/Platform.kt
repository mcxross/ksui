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
package xyz.mcxross.ksui.core.crypto

import xyz.mcxross.ksui.exception.SignatureSchemeNotSupportedException

expect fun hash(hash: Hash, data: ByteArray): ByteArray

expect fun generateMnemonic(): String

expect fun generateSeed(mnemonic: List<String>): ByteArray

expect fun generateKeyPair(seed: ByteArray, scheme: SignatureScheme): KeyPair

expect fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey

expect fun importFromMnemonic(mnemonic: String): KeyPair

expect fun importFromMnemonic(mnemonic: List<String>): KeyPair

fun generatePrivateKey(scheme: SignatureScheme): ByteArray {
  return when (scheme) {
    SignatureScheme.ED25519 -> {
      val seedPhrase = generateMnemonic().split(" ")
      val seed = generateSeed(seedPhrase)
      generateKeyPair(seed, SignatureScheme.ED25519).privateKey
    }
    else -> {
      throw SignatureSchemeNotSupportedException()
    }
  }
}
