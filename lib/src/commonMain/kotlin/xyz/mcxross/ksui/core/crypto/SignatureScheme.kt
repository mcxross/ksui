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

enum class SignatureScheme(val scheme: Byte) {
  ED25519(0x00.toByte()),
  Secp256k1(0x01.toByte()),
  Secp256r1(0x02.toByte()),
  BLS12381(0xff.toByte());

  companion object {
    private val BY_SCHEME: MutableMap<Byte, SignatureScheme> = HashMap()

    init {
      for (e in entries) {
        BY_SCHEME[e.scheme] = e
      }
    }

    fun valueOf(scheme: Byte): SignatureScheme? {
      return BY_SCHEME[scheme]
    }
  }
}
