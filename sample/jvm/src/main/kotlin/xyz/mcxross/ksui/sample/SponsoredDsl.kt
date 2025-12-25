/*
 * Copyright 2025 McXross
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
package xyz.mcxross.ksui.sample

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.dsl.sponsoredTransaction
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.model.TransactionData
import xyz.mcxross.ksui.model.sign
import xyz.mcxross.ksui.util.bcsDecode
import xyz.mcxross.ksui.util.runBlocking

@OptIn(ExperimentalEncodingApi::class)
fun main() = runBlocking {
  val sui = Sui(config = SuiConfig(SuiSettings(Network.TESTNET)))

  // Using the new sponsoredTransaction DSL
  val sponsoredResponse: SponsoredResponse =
    sponsoredTransaction(
      sui = sui,
      requestFactory = { txBytes, sender -> GasRequest(txBytes, sender) },
    ) {
      sender = ALICE_ACCOUNT.address
      gasStation {
        url = GAS_STATION_URL
        headers["X-API-Key"] = GAS_STATION_API_KEY
      }
      ptb {
        moveCall {
          target = HELLO_WORLD
          arguments = +pure(0UL)
        }
      }
    }

  // After obtaining sponsorship, the user signs the transaction
  val txData = bcsDecode<TransactionData>(Base64.decode(sponsoredResponse.txBytes))

  val userSignature =
    when (val sig = txData.sign(ALICE_ACCOUNT)) {
      is xyz.mcxross.ksui.model.Result.Ok -> sig.value
      is xyz.mcxross.ksui.model.Result.Err -> throw sig.error
    }

  // Finally, execute the transaction block with both signatures
  val response =
    sui.executeTransactionBlock(
      sponsoredResponse.txBytes,
      listOf(userSignature, sponsoredResponse.sponsorSignature),
    )

  println("Transaction response: $response")
}