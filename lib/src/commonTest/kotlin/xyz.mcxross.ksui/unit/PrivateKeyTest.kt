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
import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey
import xyz.mcxross.ksui.core.crypto.PrivateKey

class PrivateKeyTest {

  @Test
  fun testPrivateKeyGeneration() {
    val privateKey = Ed25519PrivateKey()
    assertTrue { privateKey.data.size == 32 }
  }

  @Test
  fun testPrivateKeyImportConstructor() {
    val privateKey = Ed25519PrivateKey(PRIVATE_KEY_DATA)
    assertTrue { privateKey.data.size == 32 }
    assertTrue { privateKey.export() == PRIVATE_KEY_DATA }
  }

  @Test
  fun testPrivateKeyImportFromEncoded() {
    val privateKey = PrivateKey.fromEncoded(PRIVATE_KEY_DATA)
    assertTrue { privateKey.data.size == 32 }
    assertTrue { privateKey.export() == PRIVATE_KEY_DATA }
  }
}
