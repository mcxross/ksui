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

import kotlin.test.Test
import io.grpc.Status
import io.grpc.StatusException
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import sui.rpc.v2.BcsBuilder
import sui.rpc.v2.UserSignatureBuilder
import sui.rpc.v2.VerifySignatureRequestBuilder

class SuiGrpcClientLiveTest {
  @Test
  fun rejectsInvalidTransactionSignatureOnTestnet() = runBlocking {
    val client = SuiGrpcClient.connect("fullnode.testnet.sui.io", 443)
    try {
    val message =
      BcsBuilder().apply {
        name = "TransactionData"
        value = byteArrayOf(0x00)
      }
    val signatureBytes = ByteArray(1).apply { this[0] = 0x00 }
    val userSignature =
      UserSignatureBuilder().apply {
        bcs =
          BcsBuilder().apply {
            name = "UserSignature"
            value = signatureBytes
          }
      }
    val request =
      VerifySignatureRequestBuilder().apply {
        this.message = message
        signature = userSignature
      }

    val failure =
      runCatching {
        client.signatureVerificationService.VerifySignature(request)
      }.exceptionOrNull()
    assertTrue(failure is StatusException)
    val code = (failure as StatusException).status.code
    assertTrue(code == Status.Code.INVALID_ARGUMENT || code == Status.Code.INTERNAL)
    } finally {
      client.close()
    }
  }
}
