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

import xyz.mcxross.fastkrypto.FastCryptoFfiException
import xyz.mcxross.fastkrypto.KeyPairBytes as FastKryptoKeyPairBytes
import xyz.mcxross.fastkrypto.SignatureScheme as FastKryptoSignatureScheme
import xyz.mcxross.fastkrypto.blake2b256
import xyz.mcxross.fastkrypto.ed25519PublicKeyFromPrivate
import xyz.mcxross.fastkrypto.ed25519Sign
import xyz.mcxross.fastkrypto.ed25519Verify
import xyz.mcxross.fastkrypto.mnemonicDeriveKeypair
import xyz.mcxross.fastkrypto.mnemonicDerivePrivateKey
import xyz.mcxross.fastkrypto.mnemonicGenerate
import xyz.mcxross.fastkrypto.mnemonicToSeed
import xyz.mcxross.fastkrypto.secp256k1PublicKeyFromPrivate
import xyz.mcxross.fastkrypto.secp256k1Sign
import xyz.mcxross.fastkrypto.secp256k1Verify
import xyz.mcxross.fastkrypto.secp256r1PublicKeyFromPrivate
import xyz.mcxross.fastkrypto.secp256r1Sign
import xyz.mcxross.fastkrypto.secp256r1Verify
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.core.model.Result

private const val DEFAULT_ED25519_DERIVATION_PATH = "m/44H/784H/0H/0H/0H"
private const val DEFAULT_MNEMONIC_WORDS: UInt = 12u

internal expect fun verifyPasskeyProofOfKey(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E>

actual fun hash(hash: Hash, data: ByteArray): ByteArray =
  when (hash) {
    Hash.BLAKE2B256 -> blake2b256(data)
  }

actual fun generateMnemonic(): String = mnemonicGenerate(DEFAULT_MNEMONIC_WORDS)

actual fun generateSeed(mnemonic: List<String>): ByteArray =
  mnemonicToSeed(mnemonic.joinToString(" "), "")

actual fun derivePrivateKeyFromMnemonic(
  mnemonic: List<String>,
  scheme: SignatureScheme,
  path: String,
): ByteArray {
  val phrase = mnemonic.joinToString(" ")
  return mnemonicCall {
    when (scheme) {
      SignatureScheme.ED25519 ->
        mnemonicDerivePrivateKey(
          phrase,
          "",
          FastKryptoSignatureScheme.ED25519,
          normalizeEd25519Path(path),
        )
      SignatureScheme.Secp256k1 ->
        mnemonicDerivePrivateKey(phrase, "", FastKryptoSignatureScheme.SECP256K1, path)
      SignatureScheme.Secp256r1 ->
        mnemonicDerivePrivateKey(phrase, "", FastKryptoSignatureScheme.SECP256R1, path)
      else -> throw SignatureSchemeNotSupportedException()
    }
  }
}

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey =
  when (schema) {
    SignatureScheme.ED25519 ->
      Ed25519PublicKey(privateKey.data.useThenWipe(::ed25519PublicKeyFromPrivate))
    SignatureScheme.Secp256k1 ->
      Secp256k1PublicKey(privateKey.data.useThenWipe(::secp256k1PublicKeyFromPrivate))
    SignatureScheme.Secp256r1 ->
      Secp256r1PublicKey(privateKey.data.useThenWipe(::secp256r1PublicKeyFromPrivate))
    else -> throw SignatureSchemeNotSupportedException()
  }

actual fun importFromMnemonic(mnemonic: String): KeyPair = mnemonicKeyPair(mnemonic)

actual fun importFromMnemonic(mnemonic: List<String>): KeyPair =
  mnemonicKeyPair(mnemonic.joinToString(" "))

actual fun sign(message: ByteArray, privateKey: PrivateKey): Result<ByteArray, E> =
  try {
    val signature =
      when (privateKey) {
        is Ed25519PrivateKey -> privateKey.data.useThenWipe { ed25519Sign(it, message) }
        is Secp256k1PrivateKey -> privateKey.data.useThenWipe { secp256k1Sign(it, message) }
        is Secp256r1PrivateKey -> privateKey.data.useThenWipe { secp256r1Sign(it, message) }
        else -> throw SignatureSchemeNotSupportedException()
      }
    Result.Ok(signature)
  } catch (e: FastCryptoFfiException) {
    Result.Err(e)
  } catch (e: Exception) {
    Result.Err(e)
  }

actual fun verifySignature(
  publicKey: PublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E> =
  try {
    when (publicKey) {
      is Ed25519PublicKey -> Result.Ok(ed25519Verify(publicKey.data, message, signature))
      is Secp256k1PublicKey -> Result.Ok(secp256k1Verify(publicKey.data, message, signature))
      is Secp256r1PublicKey -> Result.Ok(secp256r1Verify(publicKey.data, message, signature))
      is PasskeyPublicKey -> verifyPasskeyProofOfKey(publicKey, message, signature)
      else -> Result.Err(IllegalArgumentException("Unsupported public key type"))
    }
  } catch (e: FastCryptoFfiException) {
    Result.Err(e)
  } catch (e: Exception) {
    Result.Err(e)
  }

private fun mnemonicKeyPair(phrase: String): KeyPair {
  val keyPair: FastKryptoKeyPairBytes = mnemonicCall {
    mnemonicDeriveKeypair(
      phrase,
      "",
      FastKryptoSignatureScheme.ED25519,
      DEFAULT_ED25519_DERIVATION_PATH,
    )
  }
  return KeyPair(keyPair.privateKey, keyPair.publicKey)
}

/** Hands a private-key copy to native code, then zeroes the copy so it does not linger. */
private inline fun <T> ByteArray.useThenWipe(block: (ByteArray) -> T): T =
  try {
    block(this)
  } finally {
    fill(0)
  }

/**
 * fastkrypto rejects a bad BIP-39 checksum or a word outside the English wordlist with an
 * InvalidInput error that has no message. Report it as IllegalArgumentException, like the JS target.
 */
private inline fun <T> mnemonicCall(block: () -> T): T =
  try {
    block()
  } catch (e: FastCryptoFfiException) {
    throw IllegalArgumentException("Invalid mnemonic phrase or derivation path", e)
  }

private fun normalizeEd25519Path(path: String): String {
  val parts = path.trim().split("/")
  require(parts.size >= 2 && parts.first() == "m") { "Invalid Ed25519 derivation path: $path" }
  val normalized =
    parts.drop(1).map { segment ->
      require(segment.endsWith("'") || segment.endsWith("H")) {
        "Ed25519 derivation requires hardened path segments: $path"
      }
      val index = segment.dropLast(1).toLongOrNull()
      require(index != null && index in 0..Int.MAX_VALUE.toLong()) {
        "Invalid child index in derivation path: $segment"
      }
      "${index}H"
    }
  return "m/" + normalized.joinToString("/")
}
