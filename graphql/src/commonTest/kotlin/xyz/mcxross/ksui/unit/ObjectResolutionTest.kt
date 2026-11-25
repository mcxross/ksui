package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.client.installHttpClientForTest
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.internal.COIN_PAGE_SIZE
import xyz.mcxross.ksui.internal.GraphqlGasEnvironment
import xyz.mcxross.ksui.internal.MULTI_GET_OBJECTS_BATCH_SIZE
import xyz.mcxross.ksui.ptb.ptb

private const val DIGEST = "BqrFumAb7yjZKKQUApKjzqdgHUZY3XaTNWxNQsvjMSBc"

class ObjectResolutionTest :
  StringSpec({
    val json = Json { ignoreUnknownKeys = true }

    fun mockSui(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): Sui {
      val config = SuiConfig(SuiSettings(indexer = "https://indexer.test/graphql"))
      val ktor = HttpClient(MockEngine(handler)) { install(ContentNegotiation) { json(json) } }
      installHttpClientForTest(config, ktor)
      return Sui(config, makeDefault = false)
    }

    fun objectsResponse(id: String, ownerJson: String) =
      """{"data":{"multiGetObjects":[{"__typename":"Object","objectId":"$id","version":9,"digest":"$DIGEST","owner":$ownerJson}]}}"""

    fun HttpRequestData.variables() =
      json
        .parseToJsonElement((body as OutgoingContent.ByteArrayContent).bytes().decodeToString())
        .jsonObject["variables"]!!
        .jsonObject

    "multiGetObjects splits long ID lists into batches and keeps their order" {
      // Start above the system objects (0x5, 0x6, 0x8, ...) that the builder resolves locally.
      val ids = List(70) { "0x" + (it + 0x1000).toString(16).padStart(64, '0') }
      val batchSizes = mutableListOf<Int>()
      val sui = mockSui { request ->
        val keys = request.variables()["keys"]!!.jsonArray.map { it.jsonObject["address"]!!.jsonPrimitive.content }
        batchSizes += keys.size
        val objects =
          keys.joinToString(",") {
            """{"__typename":"Object","objectId":"$it","version":1,"digest":"$DIGEST","owner":{"__typename":"Immutable"}}"""
          }
        respond("""{"data":{"multiGetObjects":[$objects]}}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
      }
      val resolved = ptb(sui) { ids.forEach { `object`(it) } }
      batchSizes shouldBe listOf(MULTI_GET_OBJECTS_BATCH_SIZE, MULTI_GET_OBJECTS_BATCH_SIZE, 6)
      resolved.inputs.map {
        (it as CallArg.Object).arg.shouldBeInstanceOf<ObjectArg.ImmOrOwnedObject>().objectRef.reference.accountAddress.toString()
      } shouldBe ids
      sui.close()
    }

    "gas coin scans request the maximum page size" {
      var first: Int? = null
      val config = SuiConfig(SuiSettings(indexer = "https://indexer.test/graphql"))
      val ktor =
        HttpClient(MockEngine { request ->
          first = request.variables()["first"]?.jsonPrimitive?.int
          respond(
            """{"data":{"address":{"address":"0x${"1".padStart(64, '0')}","objects":{"pageInfo":{"hasNextPage":false,"endCursor":null},"nodes":[]}}}}""",
            HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, "application/json"),
          )
        }) {
          install(ContentNegotiation) { json(json) }
        }
      installHttpClientForTest(config, ktor)
      GraphqlGasEnvironment(config).coins(AccountAddress.fromString("0x1"))
      first shouldBe COIN_PAGE_SIZE
      config.close()
    }

    "consensus-owned objects resolve as shared inputs at their start version" {
      val id = "0x" + "c0".padStart(64, '0')
      val owner =
        """{"__typename":"ConsensusAddressOwner","startVersion":42,"address":{"address":"0x${"1".padStart(64, '0')}"}}"""
      val sui = mockSui {
        respond(objectsResponse(id, owner), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
      }
      val resolved = ptb(sui) { `object`(id) }
      val arg = resolved.inputs.single().shouldBeInstanceOf<CallArg.Object>().arg
      arg.shouldBeInstanceOf<ObjectArg.SharedObject>().initialSharedVersion shouldBe 42L
      sui.close()
    }

    "shared objects without an initialSharedVersion fail instead of using version 0" {
      val id = "0x" + "5a".padStart(64, '0')
      val owner = """{"__typename":"Shared","initialSharedVersion":null}"""
      val sui = mockSui {
        respond(objectsResponse(id, owner), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
      }
      val error = runCatching { ptb(sui) { `object`(id) } }.exceptionOrNull()
      error.shouldBeInstanceOf<IllegalStateException>().message!! shouldContain "initialSharedVersion"
      sui.close()
    }
  })
