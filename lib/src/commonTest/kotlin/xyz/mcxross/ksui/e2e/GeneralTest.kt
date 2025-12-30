package xyz.mcxross.ksui.e2e

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.util.runBlocking

class GeneralTest :
  StringSpec({
    "Get chain identifier" {
      runBlocking {
        val resp = sui.getChainIdentifier().expect { "Failed to get chain identifier" }
        val data = requireNotNull(resp)
        data.chainIdentifier.isNotEmpty() shouldBe true
      }
    }

    "Get reference gas price" {
      runBlocking {
        val resp = sui.getReferenceGasPrice().expect { "Failed to get reference gas price" }
        val data = requireNotNull(resp)
        val epoch = requireNotNull(data.epoch)
        requireNotNull(epoch.referenceGasPrice).toString().isEmpty() shouldBe false
      }
    }

    "Get checkpoint" {
      runBlocking {
        val resp = sui.getCheckpoint().expect { "Failed to get checkpoint" }
        requireNotNull(resp)
      }
    }

    "Get latest Sui system state" {
      runBlocking {
        val resp = sui.getLatestSuiSystemState().expect { "Failed to get latest Sui system state" }
        val data = requireNotNull(resp)
        val epoch = requireNotNull(data.epoch)
        (epoch.epochId.toString().toLong() > 0) shouldBe true
        requireNotNull(epoch.startTimestamp).toString().isNotEmpty() shouldBe true
      }
    }

    "Get protocol config" {
      runBlocking {
        val resp = sui.getProtocolConfig().expect { "Failed to get protocol config" }
        requireNotNull(resp)
      }
    }
  })
