package xyz.mcxross.ksui.e2e

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.util.runBlocking

class GeneralTest {

  @Test
  fun getChainIdentifierTest() = runBlocking {
    val resp = sui.getChainIdentifier().expect("Failed to get chain identifier")
    assertNotNull(resp, "Failed to get chain identifier")
    assertTrue { resp.isNotEmpty() }
  }

  @Test
  fun getReferenceGasPriceTest() = runBlocking {
    val resp = sui.getReferenceGasPrice().expect("Failed to get reference gas price")
    assertNotNull(resp, "Failed to get reference gas price")
    assertFalse { resp.isEmpty() }
  }

  @Test
  fun getCheckpointTest() = runBlocking {
    val resp = sui.getCheckpoint().expect("Failed to get checkpoint")
    assertNotNull(resp, "Failed to get checkpoint")
  }

  @Test
  fun getLatestSuiSystemStateTest() = runBlocking {
    val resp = sui.getLatestSuiSystemState().expect("Failed to get latest Sui system state")
    assertNotNull(resp, "Failed to get latest Sui system state")
    assertNotNull(resp.epoch, "Epoch is null")
    assertTrue { resp.epoch!!.epochId > 0 }
    assertTrue { resp.epoch!!.startTimestamp.isNotEmpty() }
  }

  @Test
  fun getProtocolConfigTest() = runBlocking {
    val resp = sui.getProtocolConfig().expect("Failed to get protocol config")
    assertNotNull(resp, "Failed to get protocol config")
  }
}
