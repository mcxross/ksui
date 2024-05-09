package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class Envelope<T>(var data: T) {
  var digest: T? = null
  var authSignature: EmptySignInfo

  init {
    this.digest = null
    this.authSignature = EmptySignInfo()
  }
}

@Serializable class EmptySignInfo
