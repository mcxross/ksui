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

import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.Result

private fun unsupported(): Nothing =
  throw UnsupportedOperationException("fastkrypto bindings are not available for tvOS")

actual fun hash(hash: Hash, data: ByteArray): ByteArray = unsupported()

actual fun generateMnemonic(): String = unsupported()

actual fun generateSeed(mnemonic: List<String>): ByteArray = unsupported()

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey =
  unsupported()

actual fun importFromMnemonic(mnemonic: String): KeyPair = unsupported()

actual fun importFromMnemonic(mnemonic: List<String>): KeyPair = unsupported()

actual fun sign(message: ByteArray, privateKey: PrivateKey): Result<ByteArray, E> = unsupported()

actual fun derivePrivateKeyFromMnemonic(
  mnemonic: List<String>,
  scheme: SignatureScheme,
  path: String,
): ByteArray = unsupported()

actual fun verifySignature(
  publicKey: PublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E> = unsupported()
