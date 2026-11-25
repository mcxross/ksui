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

import xyz.mcxross.ksui.core.Hex

/** Group order n of secp256k1 (SEC 2). Checked against BouncyCastle in the JVM tests. */
internal val SECP256K1_ORDER: ByteArray =
  Hex.fromString("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141").toByteArray()

/** Group order n of secp256r1 / P-256 (SEC 2, FIPS 186). Checked against BouncyCastle in tests. */
internal val SECP256R1_ORDER: ByteArray =
  Hex.fromString("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551").toByteArray()

private const val PRIVATE_KEY_SIZE = 32

/**
 * Random 32-byte Ed25519 secret key. Any 32 bytes are a valid Ed25519 seed, which is also how the
 * Mysten TypeScript SDK generates keys (`ed25519.utils.randomSecretKey()`).
 */
internal fun randomEd25519SecretKey(): ByteArray = secureRandomBytes(PRIVATE_KEY_SIZE)

/**
 * Random ECDSA secret key in `[1, order - 1]` by rejection sampling. A 32-byte draw falls outside
 * that range with probability below 2^-127 for both curves, so the loop practically runs once.
 */
internal fun randomEcdsaSecretKey(order: ByteArray): ByteArray {
  require(order.size == PRIVATE_KEY_SIZE) { "Curve order must be $PRIVATE_KEY_SIZE bytes" }
  while (true) {
    val candidate = secureRandomBytes(PRIVATE_KEY_SIZE)
    if (candidate.any { it != 0.toByte() } && compareUnsigned(candidate, order) < 0) {
      return candidate
    }
    candidate.fill(0)
  }
}

internal fun compareUnsigned(a: ByteArray, b: ByteArray): Int {
  require(a.size == b.size) { "Operands must have the same length" }
  for (i in a.indices) {
    val diff = (a[i].toInt() and 0xff) - (b[i].toInt() and 0xff)
    if (diff != 0) return diff
  }
  return 0
}
