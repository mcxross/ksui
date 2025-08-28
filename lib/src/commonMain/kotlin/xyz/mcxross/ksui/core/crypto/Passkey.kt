/*
 * Copyright 2025 McXross
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

import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.Result

data class PasskeyPublicKey(override val data: ByteArray) : PublicKey {
  override fun scheme(): SignatureScheme = SignatureScheme.PASSKEY

  override fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> {
    return verifySignature(this, message, signature)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as PasskeyPublicKey

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }
}

/**
 * Finds the unique public key that exists in both arrays, throws error if the common pubkey does
 * not equal to one.
 *
 * @param arr1 - The first pubkeys array.
 * @param arr2 - The second pubkeys array.
 * @return The only common pubkey in both arrays.
 */
fun findCommonPublicKey(
  arr1: List<PasskeyPublicKey>,
  arr2: List<PasskeyPublicKey>,
): PasskeyPublicKey {
  val matchingPubkeys = mutableListOf<PasskeyPublicKey>()
  for (pubkey1 in arr1) {
    for (pubkey2 in arr2) {
      if (pubkey1 == pubkey2) {
        matchingPubkeys.add(pubkey1)
      }
    }
  }

  if (matchingPubkeys.size != 1) {
    throw IllegalStateException("Expected 1 common public key, but found ${matchingPubkeys.size}")
  }
  return matchingPubkeys.first()
}
