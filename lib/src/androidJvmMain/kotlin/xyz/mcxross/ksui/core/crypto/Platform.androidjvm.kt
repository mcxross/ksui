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

import java.math.BigInteger
import java.security.SecureRandom
import java.util.ArrayList
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicCode
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.params.ParametersWithRandom
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import xyz.mcxross.ksui.exception.SignatureSchemeNotSupportedException

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

actual fun derivePrivateKeyFromMnemonic(
  mnemonic: List<String>,
  scheme: SignatureScheme,
  path: String,
): ByteArray {
  val binarySeed = generateSeed(mnemonic)

  return when (scheme) {
    SignatureScheme.ED25519 -> {
      val derivedKey = ED25519KeyDerive.createKeyByDefaultPath(binarySeed)
      derivedKey.key
    }
    SignatureScheme.Secp256k1 -> {
      derivePrivateKeyFromSeed(binarySeed, path)
    }
    SignatureScheme.Secp256r1 -> {
      derivePrivateKeyFromSeed(binarySeed, path)
    }
    else -> throw SignatureSchemeNotSupportedException()
  }
}

@Throws(SignatureSchemeNotSupportedException::class)
fun generateKeyPair(seed: ByteArray, scheme: SignatureScheme): KeyPair {
  return when (scheme) {
    SignatureScheme.ED25519 -> {
      val key: ED25519KeyDerive = ED25519KeyDerive.createKeyByDefaultPath(seed)
      val parameters = Ed25519PrivateKeyParameters(key.key)
      val publicKeyParameters = parameters.generatePublicKey()
      KeyPair(parameters.encoded, publicKeyParameters.encoded)
    }
    SignatureScheme.Secp256k1 -> {
      generateEcdsaKeyPair("secp256k1")
    }
    SignatureScheme.Secp256r1 -> {
      generateEcdsaKeyPair("secp256r1")
    }
    else -> throw NotImplementedError("Only Ed25519 and Secp256k1Ecdsa are supported at the moment")
  }
}

private fun generateEcdsaKeyPair(curveName: String): KeyPair {
  val params = SECNamedCurves.getByName(curveName)
  val domainParams = ECDomainParameters(params.curve, params.g, params.n, params.h)

  val keyPairGenerator = ECKeyPairGenerator()
  val keyGenParams = ECKeyGenerationParameters(domainParams, SecureRandom())
  keyPairGenerator.init(keyGenParams)

  val keyPair = keyPairGenerator.generateKeyPair()

  val publicKey = keyPair.public as ECPublicKeyParameters
  val privateKey = keyPair.private as ECPrivateKeyParameters

  val publicKeyBytes = publicKey.q.getEncoded(true)
  val privateKeyBytes =
    privateKey.d.toByteArray().let {
      if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else ByteArray(32 - it.size) + it
    }

  return KeyPair(privateKeyBytes, publicKeyBytes)
}

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey {
  return when (schema) {
    SignatureScheme.ED25519 -> {
      val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.data)
      val publicKeyParameters = privateKeyParameters.generatePublicKey()
      Ed25519PublicKey(publicKeyParameters.encoded)
    }
    SignatureScheme.Secp256k1 ->
      Secp256k1PublicKey(deriveEcdsaPublicKey(privateKey.data, "secp256k1"))
    SignatureScheme.Secp256r1 ->
      Secp256r1PublicKey(deriveEcdsaPublicKey(privateKey.data, "secp256r1"))
    else -> throw SignatureSchemeNotSupportedException()
  }
}

private fun deriveEcdsaPublicKey(privateKey: ByteArray, curveName: String): ByteArray {
  val ecSpec = SECNamedCurves.getByName(curveName)
  val domainParameters = ECDomainParameters(ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h)
  val privateKeyD = BigInteger(1, privateKey)
  val q = FixedPointCombMultiplier().multiply(domainParameters.g, privateKeyD)
  val publicKeyParameters = ECPublicKeyParameters(q, domainParameters)
  return publicKeyParameters.q.getEncoded(true)
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
    is Secp256k1PrivateKey -> {
      return signEcdsa(message, privateKey.data, "secp256k1")
    }
    is Secp256r1PrivateKey -> {
      return signEcdsa(message, privateKey.data, "secp256r1")
    }
    else -> throw SignatureSchemeNotSupportedException()
  }
}

private fun signEcdsa(
  message: ByteArray,
  privateKeyBytes: ByteArray,
  curveName: String,
): ByteArray {
  val params = CustomNamedCurves.getByName(curveName)
  val curveParams = ECDomainParameters(params.curve, params.g, params.n, params.h)
  val privKeyParams = ECPrivateKeyParameters(BigInteger(1, privateKeyBytes), curveParams)
  val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
  signer.init(true, ParametersWithRandom(privKeyParams, SecureRandom()))

  val messageHash = org.bouncycastle.jcajce.provider.digest.SHA256.Digest().digest(message)
  val sig = signer.generateSignature(messageHash)
  val r = sig[0]
  var s = sig[1]

  val halfOfCurveOrder = curveParams.n.shiftRight(1)
  if (s > halfOfCurveOrder) {
    s = curveParams.n.subtract(s)
  }

  val rBytes =
    r.toByteArray().let {
      if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else ByteArray(32 - it.size) + it
    }
  val sBytes =
    s.toByteArray().let {
      if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else ByteArray(32 - it.size) + it
    }

  return rBytes + sBytes
}

private fun derivePrivateKeyFromSeed(seed: ByteArray, path: String): ByteArray {
  val masterKey: DeterministicKey = HDKeyDerivation.createMasterPrivateKey(seed)
  val pathParts = path.replace("m/", "").split("/")
  var derivedKey: DeterministicKey = masterKey

  for (part in pathParts) {
    val isHardened = part.endsWith("'")
    val index = part.removeSuffix("'").toInt()
    val childNumber = ChildNumber(index, isHardened)
    derivedKey = HDKeyDerivation.deriveChildKey(derivedKey, childNumber)
  }

  return derivedKey.privKeyBytes
}

actual fun verifySignature(
  publicKey: PublicKey,
  message: ByteArray,
  signature: ByteArray
): Boolean {
  return when (publicKey) {
    is Ed25519PublicKey -> {
      val signer = org.bouncycastle.crypto.signers.Ed25519Signer()
      val publicKeyParameters = Ed25519PublicKeyParameters(publicKey.data, 0)
      signer.init(false, publicKeyParameters)
      signer.update(message, 0, message.size)
      signer.verifySignature(signature)
    }
    is Secp256k1PublicKey -> {
      verifyEcdsa(publicKey.data, "secp256k1", message, signature)
    }
    is Secp256r1PublicKey -> {
      verifyEcdsa(publicKey.data, "secp256r1", message, signature)
    }
    else -> throw IllegalArgumentException("Unsupported public key type")
  }
}

private fun verifyEcdsa(
  publicKeyBytes: ByteArray,
  curveName: String,
  message: ByteArray,
  signature: ByteArray
): Boolean {
  require(signature.size == 64) { "Signature must be 64 bytes long" }

  val messageHash = org.bouncycastle.jcajce.provider.digest.SHA256.Digest().digest(message)

  val r = BigInteger(1, signature.copyOfRange(0, 32))
  val s = BigInteger(1, signature.copyOfRange(32, 64))

  val params = CustomNamedCurves.getByName(curveName)
  val curveParams = ECDomainParameters(params.curve, params.g, params.n, params.h)

  val q = curveParams.curve.decodePoint(publicKeyBytes)
  val pubKeyParams = ECPublicKeyParameters(q, curveParams)

  val signer = ECDSASigner()
  signer.init(false, pubKeyParams)

  return signer.verifySignature(messageHash, r, s)
}
