package xyz.mcxross.ksui.account

import xyz.mcxross.ksui.core.crypto.*
import xyz.mcxross.ksui.exception.E
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Result

class Secp256r1Account(private val privateKey: Secp256r1PrivateKey) : Account() {

  var mnemonic: String = ""

  override val publicKey: Secp256r1PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress
    get() = AccountAddress.fromPublicKey(publicKey)

  override val scheme: SignatureScheme
    get() = SignatureScheme.Secp256r1

  override suspend fun sign(message: ByteArray): Result<ByteArray, E> {
    return privateKey.sign(message)
  }

  override suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
    publicKey.verify(message, signature)

  constructor(privateKey: Secp256r1PrivateKey, mnemonic: String) : this(privateKey) {
    this.mnemonic = mnemonic
  }

  override fun toString(): String {
    return "Secp256r1Account{mnemonic=$mnemonic, privKey=${privateKey.export()}, address=$address}"
  }

  companion object {
    const val DERIVATION_PATH = "m/74'/784'/0'/0/0"

    fun generate(): Secp256r1Account {
      val seedPhrase = generateMnemonic().split(" ")
      val finalPrivateKeyBytes =
        derivePrivateKeyFromMnemonic(seedPhrase, SignatureScheme.Secp256r1, DERIVATION_PATH)
      val privateKey = Secp256r1PrivateKey(finalPrivateKeyBytes)
      return Secp256r1Account(privateKey, seedPhrase.joinToString(" "))
    }
  }
}
