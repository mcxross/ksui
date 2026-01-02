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

package xyz.mcxross.ksui.grpc

import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.grpc.GrpcServer
import kotlinx.rpc.registerService
import sui.rpc.v2.SignatureVerificationService
import sui.rpc.v2.VerifySignatureRequestBuilder
import sui.rpc.v2.VerifySignatureRequest
import sui.rpc.v2.VerifySignatureResponseBuilder
import sui.rpc.v2.VerifySignatureResponse

class SuiGrpcClientTest {
  private lateinit var server: kotlinx.rpc.grpc.GrpcServer
  private lateinit var client: SuiGrpcClient
  private var port: Int = 0

  @BeforeTest
  fun setUp() {
    port = findFreePort()
    server = GrpcServer(port) { registerService<SignatureVerificationService> { TestSignatureVerificationService() } }
    server.start()
    client = SuiGrpcClient.connect("localhost", port, usePlaintext = true)
  }

  @AfterTest
  fun tearDown() {
    client.close()
    server.shutdown()
  }

  @Test
  fun usesSignatureVerificationService() = runBlocking {
    val response =
      client.signatureVerificationService.VerifySignature(VerifySignatureRequestBuilder())
    assertEquals(true, response.isValid)
  }

  private class TestSignatureVerificationService : SignatureVerificationService {
    override suspend fun VerifySignature(
      request: VerifySignatureRequest
    ): VerifySignatureResponse {
      return VerifySignatureResponseBuilder().apply { isValid = true }
    }
  }

  private fun findFreePort(): Int =
    ServerSocket(0).use { socket -> socket.localPort }
}
