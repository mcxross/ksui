package xyz.mcxross.ksui.core.crypto

import xyz.mcxross.ksui.account.PasskeyAccount

actual class PasskeyProvider {
    actual suspend fun create(name: String): PasskeyAccount {
        TODO("Not yet implemented")
    }

    actual suspend fun sign(challenge: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }
}
