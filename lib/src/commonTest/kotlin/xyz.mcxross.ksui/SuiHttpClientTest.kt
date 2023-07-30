package xyz.mcxross.ksui

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.*
import xyz.mcxross.ksui.client.suiHttpClient

class SuiHttpClientTest {
  @Test
  fun getLatestCheckpointSequenceNumberTest() {
    runBlocking {
      val mockEngine = MockEngine { request ->
        respond(
            content =
                ByteReadChannel(
                    """{"jsonrpc": "2.0", "result": "6757721", "id": 1}"""),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"))
      }
      val suiHttpClient = suiHttpClient { engine = mockEngine }
      val result = suiHttpClient.getLatestCheckpointSequenceNumber()
      assertEquals("CheckpointSequenceNumber(sequenceNumber=6757721)", result.toString())
    }
  }
}
