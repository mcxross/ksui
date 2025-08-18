package xyz.mcxross.ksui.account

import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.generateKeyPair
import xyz.mcxross.ksui.core.crypto.generateMnemonic
import xyz.mcxross.ksui.core.crypto.generateSeed
import xyz.mcxross.ksui.model.AccountAddress

class Secp256k1Account(private val privateKey: Secp256k1PrivateKey) : Account() {

  var mnemonic: String = ""

  override val publicKey: Secp256k1PublicKey
    get() = privateKey.publicKey

  override val address: AccountAddress
    get() = AccountAddress.fromPublicKey(publicKey)

  override val scheme: SignatureScheme
    get() = SignatureScheme.Secp256k1

  override fun sign(message: ByteArray): ByteArray {
    TODO("Not yet implemented")
  }

  constructor(privateKey: Secp256k1PrivateKey, mnemonic: String) : this(privateKey) {
    this.mnemonic = mnemonic
  }

  override fun toString(): String {
    return "Secp256k1Account{mnemonic=$mnemonic}"
  }

  companion object {

    /**
     * Generates a new `Secp256k1Account` using a randomly generated mnemonic phrase and seed.
     *
     * @return The new `Secp256k1Account`.
     */
    fun generate(): Secp256k1Account {
      val seedPhrase = generateMnemonic().split(" ")
      val seed = generateSeed(seedPhrase)
      val keyPair = generateKeyPair(seed, SignatureScheme.Secp256k1)
      return Secp256k1Account(Secp256k1PrivateKey(keyPair.privateKey), seedPhrase.joinToString(" "))
    }
  }

}
