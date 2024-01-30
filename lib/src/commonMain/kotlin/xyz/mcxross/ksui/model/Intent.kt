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
