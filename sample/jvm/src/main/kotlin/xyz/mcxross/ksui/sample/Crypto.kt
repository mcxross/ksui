package xyz.mcxross.ksui.sample

import java.security.SecureRandom
import java.util.*
import org.apache.commons.lang3.StringUtils
import org.bitcoinj.crypto.MnemonicCode
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

/**
 * Generate new key response.
 *
 * @param schema the schema
 * @return the key response
 * @throws SignatureSchemeNotSupportedException the signature scheme not supported exception
 */
@Throws(SignatureSchemeNotSupportedException::class)
fun generateNewKey(schema: SignatureScheme = SignatureScheme.ED25519): KeyResponse {
  val secureRandom = SecureRandom()
  val entropy = ByteArray(16)
  secureRandom.nextBytes(entropy)
  var mnemonic: List<String?>? = ArrayList()

  try {
    mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy)
  } catch (e: java.lang.Exception) {
    // MnemonicLengthException won't happen
  }

  val seed = MnemonicCode.toSeed(mnemonic, "")
  val keyPair: SuiKeyPair<*> = genSuiKeyPair(seed, schema)

  return KeyResponse(StringUtils.join(mnemonic, " "), keyPair.address())
}

@Throws(SignatureSchemeNotSupportedException::class)
private fun genSuiKeyPair(seed: ByteArray, schema: SignatureScheme): SuiKeyPair<*> {
  return when (schema) {
    SignatureScheme.ED25519 -> genED25519KeyPair(seed)
    else -> throw SignatureSchemeNotSupportedException()
  }
}

private fun genED25519KeyPair(seed: ByteArray): ED25519KeyPair {
  val key: ED25519KeyDerive = ED25519KeyDerive.createKeyByDefaultPath(seed)
  val parameters: Ed25519PrivateKeyParameters = Ed25519PrivateKeyParameters(key.key)
  val publicKeyParameters = parameters.generatePublicKey()

  return ED25519KeyPair(parameters, publicKeyParameters)
}

@Throws(SignatureSchemeNotSupportedException::class)
fun importFromMnemonic(mnemonic: String, schema: SignatureScheme = SignatureScheme.ED25519): SuiKeyPair<*> {
  // todo check mnemonic

  val seed =
    MnemonicCode.toSeed(
      listOf(*mnemonic.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()),
      "",
    )

  return genSuiKeyPair(seed, schema)
}
