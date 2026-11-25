@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package xyz.mcxross.ksui.core.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.arc4random_buf
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.Result

internal actual fun verifyPasskeyProofOfKey(
  publicKey: PasskeyPublicKey,
  message: ByteArray,
  signature: ByteArray,
): Result<Boolean, E> =
  Result.Err(
    UnsupportedOperationException("Passkey signature verification is not available on Apple")
  )

actual fun secureRandomBytes(size: Int): ByteArray {
  require(size >= 0) { "Size must be non-negative" }
  if (size == 0) return ByteArray(0)
  val out = ByteArray(size)
  out.usePinned { pinned -> arc4random_buf(pinned.addressOf(0), size.convert()) }
  return out
}
