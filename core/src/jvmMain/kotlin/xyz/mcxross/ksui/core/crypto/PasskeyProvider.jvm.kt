package xyz.mcxross.ksui.core.crypto

import xyz.mcxross.ksui.account.PasskeyAccount
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.Result

actual class PasskeyProvider {
  internal actual suspend fun create(
    name: String,
    displayName: String,
    userId: String,
    challenge: String,
  ): Result<PasskeyAccount, E> {
    TODO("Not yet implemented")
  }

  internal actual suspend fun sign(pk: ByteArray, challenge: ByteArray): Result<ByteArray, E> {
    TODO("Not yet implemented")
  }
}
