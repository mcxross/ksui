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
package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
enum class IntentScope {
  TRANSACTIONDATA,
  TRANSACTIONEFFECTS,
  CHECKPOINSUMMARY,
  PERSONALMESSAGE,
  SENDERSIGNEDTRANSACTION,
  PROOFOFPOSSESSION,
  HEADERDIGEST,
  BRIDGEEVENTUNUSED,
}

@Serializable
enum class IntentVersion {
  V0
}

@Serializable
enum class AppId {
  SUI,
  NARWAL,
}

@Serializable
data class Intent(val scope: IntentScope, val version: IntentVersion, val appId: AppId) {
  companion object {
    fun suiApp(scope: IntentScope): Intent = Intent(scope, IntentVersion.V0, AppId.SUI)

    fun suiTransaction(): Intent = Intent(IntentScope.TRANSACTIONDATA, IntentVersion.V0, AppId.SUI)

    fun personalMessage(): Intent = Intent(IntentScope.PERSONALMESSAGE, IntentVersion.V0, AppId.SUI)
  }
}

@Serializable data class IntentMessage<T>(val intent: Intent, val value: T)

// An intent abstraction. This is so as it is scoped, but can well be a top level function
// TODO: This is a placeholder for now, look back at this later
@Serializable
enum class IntentType {
  SUI_TX {
    override fun intent(scope: IntentScope, version: IntentVersion, appId: AppId): Intent =
      Intent(IntentScope.TRANSACTIONDATA, IntentVersion.V0, AppId.SUI)
  },
  SUI_APP {
    override fun intent(scope: IntentScope, version: IntentVersion, appId: AppId): Intent =
      Intent(IntentScope.TRANSACTIONEFFECTS, IntentVersion.V0, AppId.SUI)
  };

  abstract fun intent(
    scope: IntentScope = IntentScope.TRANSACTIONDATA,
    version: IntentVersion = IntentVersion.V0,
    appId: AppId = AppId.SUI,
  ): Intent
}
