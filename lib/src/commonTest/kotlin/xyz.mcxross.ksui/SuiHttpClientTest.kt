package xyz.mcxross.ksui

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.*
import xyz.mcxross.ksui.client.suiHttpClient
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.TransactionDigest

fun mockEngine(response: String): MockEngine = MockEngine {
  respond(
      content = ByteReadChannel(response),
      status = HttpStatusCode.OK,
      headers = headersOf(HttpHeaders.ContentType, "application/json"))
}

class SuiHttpClientTest {
  @Test
  fun getLatestCheckpointSequenceNumberTest() {
    runBlocking {
      val mockEngine = mockEngine("""{"jsonrpc": "2.0", "result": "6757721", "id": 1}""")
      val suiHttpClient = suiHttpClient { engine = mockEngine }
      val result = suiHttpClient.getLatestCheckpointSequenceNumber()
      assertEquals("CheckpointSequenceNumber(sequenceNumber=6757721)", result.toString())
    }
  }

  @Test
  fun getAllBalancesTest() {
    runBlocking {
      val mockEngine =
          mockEngine(
              """{"jsonrpc": "2.0", 
                "result": [{ "coinType": "0x2::sui::SUI", "coinObjectCount": 1, "totalBalance": "10000000000", "lockedBalance": {}}],
                "id": 1}""")
      val suiHttpClient = suiHttpClient { engine = mockEngine }
      val result =
          suiHttpClient.getAllBalances(
              SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b"))
      assertEquals(
          "[Balance(coinType=0x2::sui::SUI, coinObjectCount=1, totalBalance=10000000000, lockedBalance=LockedBalance)]",
          result.toString())
    }
  }

  @Test
  fun getAllCoinsTest() {
    runBlocking {
      val mockEngine =
          mockEngine(
              """{
    "jsonrpc": "2.0",
    "result": {
        "data": [{
            "coinType": "0x2::sui::SUI",
            "coinObjectId": "0x5428615b353c369f010e8f15ca4c50c81ccbade15c6b49b3ebf0b574e77124af",
            "version": "249",
            "digest": "DfdESmZgY9w9a8NS4yLtJCSAAmN8rN1jFu8Ay2Hbc6h6",
            "balance": "10000000000",
            "previousTransaction": "FnsDPHvF8UFTRZXdtfpbUXQCkkqyFMaHZEz8zhcVxvn4"
        }],
        "nextCursor": "0x5428615b353c369f010e8f15ca4c50c81ccbade15c6b49b3ebf0b574e77124af",
        "hasNextPage": false
    },
    "id": 1
}""")

      val suiHttpClient = suiHttpClient { engine = mockEngine }

      val result =
          suiHttpClient.getAllCoins(
              SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b"),
              null,
              5)

      assertEquals(
          "CoinPage(data=[CoinData(coinType=0x2::sui::SUI, coinObjectId=0x5428615b353c369f010e8f15ca4c50c81ccbade15c6b49b3ebf0b574e77124af, version=249, digest=DfdESmZgY9w9a8NS4yLtJCSAAmN8rN1jFu8Ay2Hbc6h6, balance=10000000000, lockedUntilEpoch=null, previousTransaction=FnsDPHvF8UFTRZXdtfpbUXQCkkqyFMaHZEz8zhcVxvn4)], nextCursor=0x5428615b353c369f010e8f15ca4c50c81ccbade15c6b49b3ebf0b574e77124af, hasNextPage=false)",
          result.toString())
    }
  }

  @Test
  fun getBalanceTest() {

    runBlocking {
      val mockEngine =
          mockEngine(
              """{
    "jsonrpc": "2.0",
    "result": {
        "coinType": "0x2::sui::SUI",
        "coinObjectCount": 1,
        "totalBalance": "10000000000",
        "lockedBalance": {}
    },
    "id": 1
}""")

      val suiHttpClient = suiHttpClient { engine = mockEngine }

      val result =
          suiHttpClient.getBalance(
              SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b"))

      assertEquals(
          "Balance(coinType=0x2::sui::SUI, coinObjectCount=1, totalBalance=10000000000, lockedBalance=LockedBalance)",
          result.toString())
    }
  }

  @Test
  fun getEventsTest() {
    runBlocking {
      val mockEngine =
          mockEngine(
              """{
    "jsonrpc": "2.0",
    "result": [{
        "id": {
            "txDigest": "6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL",
            "eventSeq": "0"
        },
        "packageId": "0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca",
        "transactionModule": "lending",
        "sender": "0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b",
        "type": "0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca::pool::PoolDeposit",
        "parsedJson": {
            "amount": "159926",
            "pool": "0000000000000000000000000000000000000000000000000000000000000002::sui::SUI",
            "sender": "0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b"
        },
        "bcs": "CQxPMw6AsCD4cTxkZX5pbdEVgUybmY29NVj4U5Bd4aEFVjyWRfhJ1WVMTS9jZwrN6G9KLkSQBTQHX9pXRnetR4JFAxqSBMSPzsNQrjdyFCwgkw2fAhBEBf49yJfpexE7SfGiMMJncLwNzatAe8owHovhFGvBS"
    }, {
        "id": {
            "txDigest": "6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL",
            "eventSeq": "1"
        },
        "packageId": "0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca",
        "transactionModule": "lending",
        "sender": "0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b",
        "type": "0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca::lending::DepositEvent",
        "parsedJson": {
            "amount": "159926",
            "reserve": 0,
            "sender": "0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b"
        },
        "bcs": "133AXmsiXxKPRtfPcmbdAk7p5a4zegRh9UiZRx4rPSr5Xsdod1aNE6Uw"
    }],
    "id": 1
}""")

      val suiHttpClient = suiHttpClient { engine = mockEngine }

      val result =
          suiHttpClient.getEvents(TransactionDigest("6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL"))

      assertEquals(
          "[Event(id=EventID(txDigest=6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL, eventSeq=0), packageId=0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca, transactionModule=lending, sender=0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b, type=0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca::pool::PoolDeposit, parsedJson={\"amount\":\"159926\",\"pool\":\"0000000000000000000000000000000000000000000000000000000000000002::sui::SUI\",\"sender\":\"0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b\"}, bcs=CQxPMw6AsCD4cTxkZX5pbdEVgUybmY29NVj4U5Bd4aEFVjyWRfhJ1WVMTS9jZwrN6G9KLkSQBTQHX9pXRnetR4JFAxqSBMSPzsNQrjdyFCwgkw2fAhBEBf49yJfpexE7SfGiMMJncLwNzatAe8owHovhFGvBS), Event(id=EventID(txDigest=6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL, eventSeq=1), packageId=0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca, transactionModule=lending, sender=0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b, type=0xd899cf7d2b5db716bd2cf55599fb0d5ee38a3061e7b6bb6eebf73fa5bc4c81ca::lending::DepositEvent, parsedJson={\"amount\":\"159926\",\"reserve\":0,\"sender\":\"0x2900aa3fb62d0849a5f1c0ce1b0b4779bc5d753ec28b8a1ccd12a16d9ff47a8b\"}, bcs=133AXmsiXxKPRtfPcmbdAk7p5a4zegRh9UiZRx4rPSr5Xsdod1aNE6Uw)]",
          result.toString())
    }
  }

  @Test
  fun getTransactionBlockTest() {
    runBlocking {
      val mockEngine =
          mockEngine(
              """{
    "jsonrpc": "2.0",
    "result": {
        "digest": "6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL",
        "timestampMs": "1690803278910",
        "checkpoint": "9106885"
    },
    "id": 1
}""")

      val httpClient = suiHttpClient { engine = mockEngine }

      val result =
          httpClient.getTransactionBlock(
              TransactionDigest("6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL"))

      assertEquals(
          "TransactionBlockResponse(digest=6D7xM9JsRu2rRNfDyvbZ6538qtSwVTD6pqgMrNKvXVRL, transaction=null, rawTransaction=, effects=null, events=[], objectChanges=[], balanceChanges=[], timestampMs=1690803278910, checkpoint=9106885, errors=[])",
          result.toString())
    }
  }
}
