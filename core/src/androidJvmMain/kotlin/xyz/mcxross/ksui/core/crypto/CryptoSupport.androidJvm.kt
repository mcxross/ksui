package xyz.mcxross.ksui.core.crypto

import java.security.SecureRandom
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.Result

private val secureRandom = SecureRandom()

internal actual fun verifyPasskeyProofOfKey(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E> = Result.Ok(verifyPasskeySignature(publicKey, message, signature))

actual fun secureRandomBytes(size: Int): ByteArray {
  require(size >= 0) { "Size must be non-negative" }
  return ByteArray(size).also(secureRandom::nextBytes)
}
