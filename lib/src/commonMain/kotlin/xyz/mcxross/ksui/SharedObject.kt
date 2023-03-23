package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class SharedObject(
  val objectId: String,
  val version: Int,
  val digest: String,
)
