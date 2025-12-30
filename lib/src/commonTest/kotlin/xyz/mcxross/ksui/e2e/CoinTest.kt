package xyz.mcxross.ksui.e2e

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import xyz.mcxross.ksui.SUI_TYPE
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.util.runBlocking

class CoinTest :
  StringSpec({
    val sui = TestResources.sui
    val alice = TestResources.alice

    "Get all balances" {
      runBlocking {
        val resp = sui.getAllBalances(alice.address).expect { "Failed to get all balances" }

        val data = requireNotNull(resp)
        val balances = requireNotNull(data.address.balances)
        balances.nodes.isNotEmpty() shouldBe true
        requireNotNull(balances.nodes.first().totalBalance)
        balances.nodes.first().coinType?.repr shouldBe SUI_TYPE
      }
    }

    "Get coins" {
      runBlocking {
        val resp = sui.getCoins(alice.address).expect { "Failed to get coins" }

        val data = requireNotNull(resp)
        val objects = requireNotNull(data.address.objects)
        objects.nodes.isNotEmpty() shouldBe true
      }
    }

    "Get total supply" {
      runBlocking {
        val resp = sui.getTotalSupply("0x2::sui::SUI").expect { "Failed to get total supply" }
        resp?.coinMetadata?.supply.toString() shouldBe "10000000000000000000"
      }
    }

    "Get balance" {
      runBlocking {
        val resp = sui.getBalance(alice.address).expect { "Failed to get balance" }

        val data = requireNotNull(resp)
        val balance = requireNotNull(data.address.balance)
        balance.coinType?.repr shouldBe SUI_TYPE
      }
    }

    "Get balance for a specific coin type" {
      runBlocking {
        val resp = sui.getBalance(alice.address, SUI_TYPE).expect { "Failed to get balance" }

        val data = requireNotNull(resp)
        val balance = requireNotNull(data.address.balance)
        balance.coinType?.repr shouldBe SUI_TYPE
      }
    }

    "Get balance for a non-existent coin type" {
      runBlocking {
        when (val result = sui.getBalance(alice.address, "0x2::usdt::USDT")) {
          is Result.Ok -> {
            result.value?.address?.balance?.totalBalance shouldBe "0"
          }
          is Result.Err -> {
            fail("Failed to get balance")
          }
        }
      }
    }

    "Get coin metadata" {
      runBlocking {
        val resp = sui.getCoinMetadata("0x2::sui::SUI").expect { "Failed to get coin metadata" }

        val data = requireNotNull(resp)
        val coinMetadata = requireNotNull(data.coinMetadata)
        coinMetadata.name shouldNotBe null
        coinMetadata.symbol shouldNotBe null
        requireNotNull(coinMetadata.address)

        coinMetadata.decimals shouldBe 9
      }
    }
  })
