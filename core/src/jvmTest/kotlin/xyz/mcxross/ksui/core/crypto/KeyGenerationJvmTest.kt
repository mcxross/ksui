package xyz.mcxross.ksui.core.crypto

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import org.bouncycastle.crypto.ec.CustomNamedCurves
import xyz.mcxross.ksui.core.model.Result

class KeyGenerationJvmTest :
  StringSpec({
    "curve order constants match BouncyCastle" {
      BigInteger(1, SECP256K1_ORDER) shouldBe CustomNamedCurves.getByName("secp256k1").n
      BigInteger(1, SECP256R1_ORDER) shouldBe CustomNamedCurves.getByName("secp256r1").n
    }

    "generated secp256k1 and secp256r1 keys derive public keys and sign" {
      val keys: List<PrivateKey> = List(3) { Secp256k1PrivateKey() } + List(3) { Secp256r1PrivateKey() }
      keys.map { it.data.toList() }.toSet() shouldHaveSize 6
      for (key in keys) {
        key.publicKey.data shouldHaveSize 33
        val message = "ksui".encodeToByteArray()
        val signature = (key.sign(message) as Result.Ok).value
        (key.publicKey.verify(message, signature) as Result.Ok).value shouldBe true
      }
    }
  })
