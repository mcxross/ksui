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
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.exception.SignatureSchemeNotSupportedException
import xyz.mcxross.ksui.model.Result

private const val DEFAULT_ED25519_DERIVATION_PATH = "m/44H/784H/0H/0H/0H"
private const val DEFAULT_MNEMONIC_WORDS: UInt = 12u

actual fun hash(hash: Hash, data: ByteArray): ByteArray {
  return when (hash) {
    Hash.BLAKE2B256 -> blake2b256(data)
  }
}

actual fun generateMnemonic(): String {
  return mnemonicGenerate(DEFAULT_MNEMONIC_WORDS)
}

actual fun generateSeed(mnemonic: List<String>): ByteArray {
  return mnemonicToSeed(mnemonic.joinToString(" "), "")
}

actual fun derivePrivateKeyFromMnemonic(
  mnemonic: List<String>,
  scheme: SignatureScheme,
  path: String,
): ByteArray {
  return when (scheme) {
    SignatureScheme.ED25519 -> {
      mnemonicDerivePrivateKey(
        mnemonic.joinToString(" "),
        "",
        FastKryptoSignatureScheme.ED25519,
        DEFAULT_ED25519_DERIVATION_PATH,
      )
    }
    SignatureScheme.Secp256k1 -> {
      mnemonicDerivePrivateKey(
        mnemonic.joinToString(" "),
        "",
        FastKryptoSignatureScheme.SECP256K1,
        path,
      )
    }
    SignatureScheme.Secp256r1 -> {
      mnemonicDerivePrivateKey(
        mnemonic.joinToString(" "),
        "",
        FastKryptoSignatureScheme.SECP256R1,
        path,
      )
    }
    else -> throw SignatureSchemeNotSupportedException()
  }
}

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey {
  return when (schema) {
    SignatureScheme.ED25519 -> {
      Ed25519PublicKey(ed25519PublicKeyFromPrivate(privateKey.data))
    }
    SignatureScheme.Secp256k1 -> Secp256k1PublicKey(secp256k1PublicKeyFromPrivate(privateKey.data))
    SignatureScheme.Secp256r1 -> Secp256r1PublicKey(secp256r1PublicKeyFromPrivate(privateKey.data))
    else -> throw SignatureSchemeNotSupportedException()
  }
}

actual fun importFromMnemonic(mnemonic: String): KeyPair {
  return mnemonicKeyPair(mnemonic)
}

actual fun importFromMnemonic(mnemonic: List<String>): KeyPair {
  return mnemonicKeyPair(mnemonic.joinToString(" "))
}

actual fun sign(message: ByteArray, privateKey: PrivateKey): Result<ByteArray, E> {
  return try {
    val signature =
      when (privateKey) {
        is Ed25519PrivateKey -> {
          ed25519Sign(privateKey.data, message)
        }
        is Secp256k1PrivateKey -> {
          secp256k1Sign(privateKey.data, message)
        }
        is Secp256r1PrivateKey -> {
          secp256r1Sign(privateKey.data, message)
        }
        else -> throw SignatureSchemeNotSupportedException()
      }
    Result.Ok(signature)
  } catch (e: FastCryptoFfiException) {
    Result.Err(e)
  } catch (e: Exception) {
    Result.Err(e)
  }
}

actual fun verifySignature(
  publicKey: PublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E> {
  return try {
    val valid =
      when (publicKey) {
        is Ed25519PublicKey -> {
          ed25519Verify(publicKey.data, message, signature)
        }
        is Secp256k1PublicKey -> {
          secp256k1Verify(publicKey.data, message, signature)
        }
        is Secp256r1PublicKey -> {
          secp256r1Verify(publicKey.data, message, signature)
        }
        else -> return Result.Err(IllegalArgumentException("Unsupported public key type"))
      }
    Result.Ok(valid)
  } catch (e: FastCryptoFfiException) {
    Result.Err(e)
  } catch (e: Exception) {
    Result.Err(e)
  }
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
