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
  V0,
}

@Serializable
enum class AppId {
  SUI,
  NARWAL,
}

@Serializable
data class Intent(val scope: IntentScope, val version: IntentVersion, val appId: AppId)

@Serializable data class IntentMessage<T>(val intent: Intent, val message: T)

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
