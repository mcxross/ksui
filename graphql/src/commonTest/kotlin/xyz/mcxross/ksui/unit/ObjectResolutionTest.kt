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
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.client.installHttpClientForTest
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.model.ObjectArg
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
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
