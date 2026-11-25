package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings

class DefaultClientTest :
  StringSpec({
    "closing the default client clears SuiKit.client with a clear error" {
      val sui = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)), makeDefault = true)
      SuiKit.client shouldBeSameInstanceAs sui
      sui.close()
      val error = runCatching { SuiKit.client }.exceptionOrNull()
      error.shouldBeInstanceOf<IllegalStateException>().message!! shouldContain
        "No default Sui client"
    }

    "closing a client that is not the default leaves the default alone" {
      val default = Sui(SuiConfig(SuiSettings(network = Network.TESTNET)), makeDefault = true)
      val other = Sui(SuiConfig(SuiSettings(network = Network.DEVNET)), makeDefault = false)
      other.close()
      SuiKit.client shouldBeSameInstanceAs default
      default.close()
    }
  })
