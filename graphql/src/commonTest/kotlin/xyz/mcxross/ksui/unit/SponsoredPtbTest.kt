/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.Digest
import xyz.mcxross.ksui.core.model.ObjectDigest
import xyz.mcxross.ksui.core.model.ObjectReference
import xyz.mcxross.ksui.core.model.Reference
import xyz.mcxross.ksui.core.model.SponsoredTransactionPolicy
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.ptb.ptb
import xyz.mcxross.ksui.dsl.sponsoredTransaction

@Serializable data class MockGasRequest(val txBytes: String, val sender: String)

@Serializable data class MockSponsoredResponse(val txBytes: String, val sponsorSignature: String)

class SponsoredPtbTest :
  StringSpec({
    "testSponsoredTransactionDsl" {
      val senderAddress = AccountAddress.fromString("0x123")
      val sponsorAddress = AccountAddress.fromString("0x456")
      val payment =
        ObjectReference(
          reference = Reference(sponsorAddress),
          version = 1,
          digest = ObjectDigest(Digest(ByteArray(32) { 1 })),
        )
      val transaction =
        TransactionData.programmableAllowSponsor(
          sender = senderAddress,
          gasPayment = listOf(payment),
          pt = ptb { pure(100UL) },
          gasBudget = 1_000_000UL,
          gasPrice = 1_000UL,
          gasSponsor = sponsorAddress,
        )
      val mockResponse =
        MockSponsoredResponse(Base64.encode(Bcs.encodeToByteArray(transaction)), "mockSignature")
      val jsonResponse = Json.encodeToString(MockSponsoredResponse.serializer(), mockResponse)

      val mockEngine = MockEngine { _ ->
        respond(
          content = jsonResponse,
          status = HttpStatusCode.OK,
          headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
      }

      val mockClient = HttpClient(mockEngine) { install(ContentNegotiation) { json() } }

      val sui = Sui(SuiConfig())

      val result: MockSponsoredResponse =
        sponsoredTransaction(
          sui = sui,
          requestFactory = { txBytes, sender -> MockGasRequest(txBytes, sender) },
          policy =
            SponsoredTransactionPolicy(
              maxGasBudget = 2_000_000UL,
              maxGasPrice = 2_000UL,
              expectedSponsor = sponsorAddress,
            ),
          responseTransactionBytes = { it.txBytes },
          httpClient = mockClient,
        ) {
          sender = senderAddress
          gasStation {
            url = "https://example.com/gas"
            headers["Authorization"] = "Bearer token"
          }
          ptb { pure(100UL) }
        }

      result.txBytes shouldBe mockResponse.txBytes
      result.sponsorSignature shouldBe "mockSignature"
    }
  })
