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
  return when (scheme) {
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

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey =
  when (schema) {
    SignatureScheme.ED25519 -> Ed25519PublicKey(ed25519PublicKeyFromPrivate(privateKey.data))
    SignatureScheme.Secp256k1 -> Secp256k1PublicKey(secp256k1PublicKeyFromPrivate(privateKey.data))
    SignatureScheme.Secp256r1 -> Secp256r1PublicKey(secp256r1PublicKeyFromPrivate(privateKey.data))
    else -> throw SignatureSchemeNotSupportedException()
  }

actual fun importFromMnemonic(mnemonic: String): KeyPair = mnemonicKeyPair(mnemonic)

actual fun importFromMnemonic(mnemonic: List<String>): KeyPair =
  mnemonicKeyPair(mnemonic.joinToString(" "))

actual fun sign(message: ByteArray, privateKey: PrivateKey): Result<ByteArray, E> =
  try {
    val signature =
      when (privateKey) {
        is Ed25519PrivateKey -> ed25519Sign(privateKey.data, message)
        is Secp256k1PrivateKey -> secp256k1Sign(privateKey.data, message)
        is Secp256r1PrivateKey -> secp256r1Sign(privateKey.data, message)
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
  val keyPair: FastKryptoKeyPairBytes =
    mnemonicDeriveKeypair(
      phrase,
      "",
      FastKryptoSignatureScheme.ED25519,
      DEFAULT_ED25519_DERIVATION_PATH,
    )
  return KeyPair(keyPair.privateKey, keyPair.publicKey)
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
