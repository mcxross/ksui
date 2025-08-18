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

import java.security.SecureRandom
import java.util.ArrayList
import org.bitcoinj.crypto.MnemonicCode
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import xyz.mcxross.ksui.exception.SignatureSchemeNotSupportedException
import java.math.BigInteger
import java.security.Security

actual fun hash(hash: Hash, data: ByteArray): ByteArray {
  return when (hash) {
    Hash.BLAKE2B256 -> Blake2b.Blake2b256().digest(data)
  }
}

actual fun generateMnemonic(): String {
  val secureRandom = SecureRandom()
  val entropy = ByteArray(16)
  secureRandom.nextBytes(entropy)
  var mnemonic: List<String> = ArrayList()

  try {
    mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy)
  } catch (e: java.lang.Exception) {
    // MnemonicLengthException won't happen
  }
  return mnemonic.joinToString(" ")
}

actual fun generateSeed(mnemonic: List<String>): ByteArray {
  return MnemonicCode.toSeed(mnemonic, "")
}

@Throws(SignatureSchemeNotSupportedException::class)
actual fun generateKeyPair(seed: ByteArray, scheme: SignatureScheme): KeyPair {
  return when (scheme) {
    SignatureScheme.ED25519 -> {
      val key: ED25519KeyDerive = ED25519KeyDerive.createKeyByDefaultPath(seed)
      val parameters = Ed25519PrivateKeyParameters(key.key)
      val publicKeyParameters = parameters.generatePublicKey()
      KeyPair(parameters.encoded, publicKeyParameters.encoded)
    }
    SignatureScheme.Secp256k1 -> {
      generateSecp256k1KeyPair()
    }
    else -> throw NotImplementedError("Only Ed25519 and Secp256k1Ecdsa are supported at the moment")
  }
}

fun generateSecp256k1KeyPair(): KeyPair {
  val params = SECNamedCurves.getByName("secp256k1")
  val domainParams = ECDomainParameters(params.curve, params.g, params.n, params.h)

  val keyPairGenerator = ECKeyPairGenerator()
  val keyGenParams = ECKeyGenerationParameters(domainParams, SecureRandom())
  keyPairGenerator.init(keyGenParams)

  val keyPair = keyPairGenerator.generateKeyPair()

  val publicKey = keyPair.public as ECPublicKeyParameters
  val privateKey = keyPair.private as ECPrivateKeyParameters

  val publicKeyBytes = publicKey.q.getEncoded(false)
  val privateKeyBytes = privateKey.d.toByteArray()

  val normalizedPrivateKeyBytes = if (privateKeyBytes.size == 33 && privateKeyBytes[0].toInt() == 0) {
    privateKeyBytes.copyOfRange(1, 33)
  } else {
    privateKeyBytes
  }

  return KeyPair(normalizedPrivateKeyBytes, publicKeyBytes)
}

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey {
  return when (schema) {
    SignatureScheme.ED25519 -> {
      val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.data)
      val publicKeyParameters = privateKeyParameters.generatePublicKey()
      Ed25519PublicKey(publicKeyParameters.encoded)
    }
    SignatureScheme.Secp256k1 -> {
      val data = generateSecp256k1PublicKey(privateKey.data)
      Secp256k1PublicKey(data)
    }
    else -> throw SignatureSchemeNotSupportedException()
  }
}

@Throws(SignatureSchemeNotSupportedException::class)
actual fun importFromMnemonic(mnemonic: String): KeyPair {
  return importFromMnemonic(mnemonic.split(" "))
}

actual fun importFromMnemonic(mnemonic: List<String>): KeyPair {
  val seed = MnemonicCode.toSeed(mnemonic, "")
  return generateKeyPair(seed, SignatureScheme.ED25519)
}

actual fun sign(message: ByteArray, privateKey: PrivateKey): ByteArray {
  when (privateKey) {
    is Ed25519PrivateKey -> {
      val signer = org.bouncycastle.crypto.signers.Ed25519Signer()
      val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.data, 0)
      signer.init(true, privateKeyParameters)
      signer.update(message, 0, message.size)
      return signer.generateSignature()
    }
    else -> throw SignatureSchemeNotSupportedException()
  }
}

fun generateSecp256k1PublicKey(privateKey: ByteArray): ByteArray {
  Security.addProvider(BouncyCastleProvider())
  val ecSpec = SECNamedCurves.getByName("secp256k1")
  val domainParameters = ECDomainParameters(ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h)
  val privateKeyD = BigInteger(1, privateKey)
  val q = FixedPointCombMultiplier().multiply(domainParameters.g, privateKeyD)
  val publicKeyParameters = ECPublicKeyParameters(q, domainParameters)
  return publicKeyParameters.q.getEncoded(false)
}

