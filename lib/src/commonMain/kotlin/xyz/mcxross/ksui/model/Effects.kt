package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class AtVersion(val objectId: String, val sequenceNumber: Long)

@Serializable
data class Effects(
  val messageVersion: String,
  val status: Status,
  val executedEpoch: String,
  val gasUsed: GasUsed,
  val modifiedAtVersions: List<AtVersion>,
  val sharedObjects: List<SharedObject> = emptyList(),
  val transactionDigest: String,
  val created: List<Object> = emptyList(),
  val mutated: List<Object> = emptyList(),
  val gasObject: GasObject,
  val eventsDigest: String = "",
  val dependencies: List<String>
)
