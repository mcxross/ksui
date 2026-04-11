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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.ByteString
import kotlinx.rpc.grpc.GrpcStatusException
import sui.rpc.v2.BcsInternal
import sui.rpc.v2.UserSignatureInternal
import sui.rpc.v2.VerifySignatureRequestInternal

class SuiGrpcClientLiveTest {
  @Test
  fun rejectsInvalidTransactionSignatureOnTestnet() = runBlocking {
    val client = SuiGrpcClient.connect("fullnode.testnet.sui.io", 443)
    try {
      val message =
        BcsInternal().apply {
          name = "TransactionData"
          value = ByteString(byteArrayOf(0x00))
        }
      val signatureBytes = ByteArray(1).apply { this[0] = 0x00 }
      val userSignature =
        UserSignatureInternal().apply {
          bcs =
            BcsInternal().apply {
              name = "UserSignature"
              value = ByteString(signatureBytes)
            }
        }
      val request =
        VerifySignatureRequestInternal().apply {
          this.message = message
          signature = userSignature
        }

      val failure =
        runCatching {
          client.signatureVerificationService.VerifySignature(request)
        }.exceptionOrNull()
      val grpcFailure = assertNotNull(failure as? GrpcStatusException)
      val statusText = grpcFailure.message.orEmpty()
      assertTrue(statusText.contains("INVALID_ARGUMENT") || statusText.contains("INTERNAL"))
    } finally {
      client.close()
    }
  }
}
