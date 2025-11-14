package xyz.mcxross.ksui.e2e

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.mcxross.ksui.SUI_TYPE
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.util.runBlocking
import kotlin.test.assertNull
import kotlin.test.fail

class CoinTest {

  private val sui = TestResources.sui
  private val alice = TestResources.alice

  @Test
  fun getAllBalancesTest() = runBlocking {
    val resp = sui.getAllBalances(alice.address).expect { "Failed to get all balances" }

    assertNotNull(resp, "Failed to get all balances")
    assertNotNull(resp.address, "Balances are null")

    assertTrue { resp.address.balances?.nodes?.isNotEmpty() ?: false }
    assertTrue { resp.address.balances?.nodes?.first()?.totalBalance != null }
    assertTrue { resp.address.balances?.nodes?.first()?.coinType?.repr == SUI_TYPE }
  }

  @Test
  fun getCoinsTest() = runBlocking {
    val resp = sui.getCoins(alice.address).expect { "Failed to get coins" }

    assertNotNull(resp, "Failed to get coins")
    assertNotNull(resp.address, "Coins are null")

    assertTrue { resp.address.objects?.nodes?.isNotEmpty() ?: false }
  }

  @Test
  fun getTotalSupplyTest() = runBlocking {
    val resp = sui.getTotalSupply("0x2::sui::SUI").expect { "Failed to get total supply" }
    assertEquals("10000000000000000000", resp?.coinMetadata?.supply.toString())
  }

  @Test
  fun getBalanceTest() = runBlocking {
    val resp = sui.getBalance(alice.address).expect { "Failed to get balance" }

    assertNotNull(resp, "Failed to get balance")
    assertNotNull(resp.address, "Balance is null")

    assertTrue { resp.address.balance != null }
    assertTrue { resp.address.balance!!.coinType?.repr == SUI_TYPE }
  }

  @Test
  fun getBalanceTypeSpecifiedTest() = runBlocking {
    val resp = sui.getBalance(alice.address, SUI_TYPE).expect { "Failed to get balance" }

    assertNotNull(resp, "Failed to get balance")
    assertNotNull(resp.address, "Balance is null")

    assertTrue { resp.address.balance != null }
    assertTrue { resp.address.balance!!.coinType?.repr == SUI_TYPE }
  }

  @Test
  fun getBalanceNonExistentTest() = runBlocking {
    when (val result = sui.getBalance(alice.address, "0x2::usdt::USDT")) {
      is Result.Ok -> {
        assertEquals("0", result.value?.address?.balance?.totalBalance)
      }
      is Result.Err -> {
        fail("Failed to get balance")
      }
    }

  }

  @Test
  fun getCoinMetadataTest() = runBlocking {
    val resp = sui.getCoinMetadata("0x2::sui::SUI").expect { "Failed to get coin metadata" }

    assertNotNull(resp, "Failed to get coin metadata")
    assertNotNull(resp.coinMetadata, "Coin metadata is null")
    assertNotNull(resp.coinMetadata.name, "Coin name is null")
    assertNotNull(resp.coinMetadata.symbol, "Coin symbol is null")
    assertNotNull(resp.coinMetadata.address, "Coin type is null")

    assertEquals(resp.coinMetadata.decimals, 9, "Coin decimals are not 9")
  }
}
