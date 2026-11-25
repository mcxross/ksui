package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256k1PrivateKey
import xyz.mcxross.ksui.core.crypto.Secp256r1PrivateKey
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.FaucetConfig
import xyz.mcxross.ksui.core.model.FullNodeConfig
import xyz.mcxross.ksui.core.model.IndexerConfig

class DisplayAndParsingSafetyTest :
  StringSpec({
    "public key toString is labelled and never looks like the account address" {
      val keys =
        listOf(
          Ed25519PrivateKey(ByteArray(32) { (it + 1).toByte() }).publicKey,
          Secp256k1PrivateKey(ByteArray(32) { (it + 1).toByte() }).publicKey,
          Secp256r1PrivateKey(ByteArray(32) { (it + 1).toByte() }).publicKey,
        )
      for (key in keys) {
        val shown = key.toString()
        shown shouldBe "${key::class.simpleName}(${Hex(key.data)})"
        shown shouldNotContain AccountAddress.fromPublicKey(key).toString()
      }
    }

    "address parsing rejects sign characters that String.toInt(16) would accept" {
      for (bad in listOf("0x" + "-1".repeat(32), "0x" + "+f".repeat(32), "-1".repeat(32))) {
        runCatching { AccountAddress.fromString(bad) }
          .exceptionOrNull()
          .shouldBeInstanceOf<IllegalArgumentException>()
      }
    }

    "address parsing still accepts mixed-case hex" {
      val raw = "aB".repeat(32)
      AccountAddress.fromString("0x$raw").toString() shouldBe "0x${raw.lowercase()}"
    }

    "config toString redacts header values and the faucet token" {
      val secret = "super-secret-value"
      val shown =
        listOf(
          FullNodeConfig(headers = mapOf("x-api-key" to secret)).toString(),
          IndexerConfig(headers = mapOf("Authorization" to "Bearer $secret")).toString(),
          FaucetConfig(headers = mapOf("x-api-key" to secret), authToken = secret).toString(),
        )
      shown.forEach { it shouldNotContain secret }
      shown[0] shouldBe "FullNodeConfig(headers={x-api-key=***})"
      shown[2] shouldBe "FaucetConfig(headers={x-api-key=***}, authToken=***)"
      FaucetConfig().toString() shouldBe "FaucetConfig(headers=null, authToken=null)"
    }

    "config equality is unchanged by the toString override" {
      FullNodeConfig(mapOf("a" to "1")) shouldBe FullNodeConfig(mapOf("a" to "1"))
      FaucetConfig(authToken = "x") shouldNotBe FaucetConfig(authToken = "y")
    }
  })
