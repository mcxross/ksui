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
package xyz.mcxross.ksui.prebuilt

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.ObjectArg
import xyz.mcxross.ksui.model.ObjectId
import xyz.mcxross.ksui.model.Option

class Clock(val sui: Sui) {
  suspend fun refresh(id: String? = null): Option<ObjectArg.SharedObject> {

    val resolvedId =
      id
        ?: when (sui.config.network) {
          Network.DEVNET -> "0x0000000000000000000000000000000000000000000000000000000000000006"
          Network.TESTNET -> "0x0000000000000000000000000000000000000000000000000000000000000006"
          Network.MAINNET -> "0x0000000000000000000000000000000000000000000000000000000000000006"
          Network.LOCAL -> throw Exception("Local network not supported")
          Network.CUSTOM -> throw Exception("Custom network not supported")
        }

    return when (val pool = sui.getObject(resolvedId)) {
      is Option.Some -> {
        Option.Some(
          ObjectArg.SharedObject(
            id =
              ObjectId(
                AccountAddress.fromString(
                  pool.value?.`object`?.objectId ?: throw Exception("No object id found")
                )
              ),
            initialSharedVersion = 1L,
            mutable = false,
          )
        )
      }
      Option.None -> Option.None
    }
  }
}
