package xyz.mcxross.ksui.core.account

import xyz.mcxross.ksui.core.crypto.Secp256r1PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256r1PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.derivePrivateKeyFromMnemonic
import xyz.mcxross.ksui.core.crypto.generateMnemonic
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Result

class Secp256r1Account(
  private val privateKey: Secp256r1PrivateKey,
  val mnemonic: String,
) : Account() {

  constructor(privateKey: Secp256r1PrivateKey) : this(privateKey, "")

  override val publicKey: Secp256r1PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress by lazy { AccountAddress.fromPublicKey(publicKey) }

  override val scheme: SignatureScheme
    get() = SignatureScheme.Secp256r1

  override suspend fun sign(message: ByteArray): Result<ByteArray, E> = privateKey.sign(message)

  override suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    publicKey.verify(message, signature)

  override fun close() {
    privateKey.close()
  }

  override fun toString(): String = "Secp256r1Account{address=$address}"

  companion object {
    const val DERIVATION_PATH = "m/74'/784'/0'/0/0"

    fun generate(): Secp256r1Account {
      val seedPhrase = generateMnemonic().split(" ")
      val finalPrivateKeyBytes =
        derivePrivateKeyFromMnemonic(seedPhrase, SignatureScheme.Secp256r1, DERIVATION_PATH)
      val privateKey = Secp256r1PrivateKey(finalPrivateKeyBytes)
      finalPrivateKeyBytes.fill(0)
      return Secp256r1Account(privateKey, seedPhrase.joinToString(" "))
    }
  }
}
