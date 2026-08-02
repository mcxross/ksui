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

import kotlin.js.JsModule
import kotlin.js.JsNonModule
import kotlin.js.definedExternally
import kotlin.js.unsafeCast
import org.khronos.webgl.Uint8Array
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.core.model.Result

private const val DEFAULT_ED25519_DERIVATION_PATH = "m/44'/784'/0'/0'/0'"

@JsModule("@noble/hashes/blake2.js")
@JsNonModule
private external object Blake2Module {
  fun blake2b(message: Uint8Array, options: dynamic = definedExternally): Uint8Array
}

@JsModule("@noble/hashes/hmac.js")
@JsNonModule
private external object HmacModule {
  fun hmac(hash: dynamic, key: Uint8Array, message: Uint8Array): Uint8Array
}

@JsModule("@noble/hashes/sha2.js")
@JsNonModule
private external object Sha2Module {
  val sha512: dynamic
}

@JsModule("@noble/curves/ed25519.js")
@JsNonModule
private external object Ed25519Module {
  val ed25519: dynamic
}

@JsModule("@scure/bip39")
@JsNonModule
private external object Bip39Module {
  fun generateMnemonic(wordlist: Array<String>, strength: Int = definedExternally): String

  fun mnemonicToSeedSync(
    mnemonic: String,
    passphrase: String = definedExternally,
  ): Uint8Array
}

@JsModule("@scure/bip39/wordlists/english.js")
@JsNonModule
private external object EnglishWordlistModule {
  val wordlist: Array<String>
}

actual fun hash(hash: Hash, data: ByteArray): ByteArray =
  when (hash) {
    Hash.BLAKE2B256 -> {
      val options = js("({ dkLen: 32 })")
      Blake2Module.blake2b(data.toUint8Array(), options).toByteArray()
    }
  }

actual fun generateMnemonic(): String = Bip39Module.generateMnemonic(EnglishWordlistModule.wordlist, 128)

actual fun generateSeed(mnemonic: List<String>): ByteArray =
  Bip39Module.mnemonicToSeedSync(mnemonic.joinToString(" "), "").toByteArray()

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey =
  when (schema) {
    SignatureScheme.ED25519 ->
      Ed25519PublicKey(
        Ed25519Module.ed25519
          .getPublicKey(privateKey.data.toUint8Array())
          .unsafeCast<Uint8Array>()
          .toByteArray()
      )
    else -> throw SignatureSchemeNotSupportedException()
  }

actual fun importFromMnemonic(mnemonic: String): KeyPair = mnemonicKeyPair(mnemonic)

actual fun importFromMnemonic(mnemonic: List<String>): KeyPair =
  mnemonicKeyPair(mnemonic.joinToString(" "))

actual fun sign(message: ByteArray, privateKey: PrivateKey): Result<ByteArray, E> =
  try {
    when (privateKey) {
      is Ed25519PrivateKey ->
        Result.Ok(
          Ed25519Module.ed25519
            .sign(message.toUint8Array(), privateKey.data.toUint8Array())
            .unsafeCast<Uint8Array>()
            .toByteArray()
        )
      else -> throw SignatureSchemeNotSupportedException()
    }
  } catch (e: Exception) {
    Result.Err(e)
  }

actual fun derivePrivateKeyFromMnemonic(
  mnemonic: List<String>,
  scheme: SignatureScheme,
  path: String,
): ByteArray =
  when (scheme) {
    SignatureScheme.ED25519 ->
      deriveEd25519PrivateKey(
        Bip39Module.mnemonicToSeedSync(mnemonic.joinToString(" "), "").toByteArray(),
        DEFAULT_ED25519_DERIVATION_PATH,
      )
    else -> throw SignatureSchemeNotSupportedException()
  }

actual fun verifySignature(
  publicKey: PublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E> =
  try {
    when (publicKey) {
      is Ed25519PublicKey ->
        Result.Ok(
          Ed25519Module.ed25519
            .verify(
              signature.toUint8Array(),
              message.toUint8Array(),
              publicKey.data.toUint8Array(),
            )
            .unsafeCast<Boolean>()
        )
      else -> throw SignatureSchemeNotSupportedException()
    }
  } catch (e: Exception) {
    Result.Err(e)
  }

private fun mnemonicKeyPair(phrase: String): KeyPair {
  val privateKey =
    deriveEd25519PrivateKey(
      Bip39Module.mnemonicToSeedSync(phrase, "").toByteArray(),
      DEFAULT_ED25519_DERIVATION_PATH,
    )
  val publicKey =
    Ed25519Module.ed25519
      .getPublicKey(privateKey.toUint8Array())
      .unsafeCast<Uint8Array>()
      .toByteArray()
  return KeyPair(privateKey, publicKey)
}

private fun deriveEd25519PrivateKey(seed: ByteArray, path: String): ByteArray {
  var digest =
    HmacModule.hmac(
        Sha2Module.sha512,
        "ed25519 seed".encodeToByteArray().toUint8Array(),
        seed.toUint8Array(),
      )
      .toByteArray()
  var key = digest.copyOfRange(0, 32)
  var chainCode = digest.copyOfRange(32, 64)

  for (component in path.split('/').drop(1)) {
    require(component.endsWith("'")) { "Ed25519 derivation requires hardened path components" }
    val child = component.dropLast(1).toLong() + 0x80000000L
    val data = ByteArray(37)
    key.copyInto(data, destinationOffset = 1)
    data[33] = (child shr 24).toByte()
    data[34] = (child shr 16).toByte()
    data[35] = (child shr 8).toByte()
    data[36] = child.toByte()
    digest =
      HmacModule.hmac(Sha2Module.sha512, chainCode.toUint8Array(), data.toUint8Array()).toByteArray()
    key = digest.copyOfRange(0, 32)
    chainCode = digest.copyOfRange(32, 64)
  }
  return key
}

private fun ByteArray.toUint8Array(): Uint8Array =
  Uint8Array(size).also { result -> result.set(toTypedArray()) }

private fun Uint8Array.toByteArray(): ByteArray =
  ByteArray(length) { index -> asDynamic()[index].unsafeCast<Int>().toByte() }
