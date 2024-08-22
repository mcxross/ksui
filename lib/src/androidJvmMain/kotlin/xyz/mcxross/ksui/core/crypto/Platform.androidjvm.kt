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
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.jcajce.provider.digest.Blake2b
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

@Throws(SignatureSchemeNotSupportedException::class)
actual fun generateKeyPair(seed: ByteArray, scheme: SignatureScheme): KeyPair {
  return when (scheme) {
    SignatureScheme.ED25519 -> {
      val key: ED25519KeyDerive = ED25519KeyDerive.createKeyByDefaultPath(seed)
      val parameters = Ed25519PrivateKeyParameters(key.key)
      val publicKeyParameters = parameters.generatePublicKey()
      KeyPair(parameters.encoded, publicKeyParameters.encoded)
    }
    else -> throw SignatureSchemeNotSupportedException()
  }
}

actual fun derivePublicKey(privateKey: PrivateKey, schema: SignatureScheme): PublicKey {
  return when (schema) {
    SignatureScheme.ED25519 -> {
      val privateKeyParameters = Ed25519PrivateKeyParameters(privateKey.data)
      val publicKeyParameters = privateKeyParameters.generatePublicKey()
      Ed25519PublicKey(publicKeyParameters.encoded)
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
