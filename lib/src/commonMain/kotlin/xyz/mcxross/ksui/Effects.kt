package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class Effects(
  val status: Status,
  val executedEpoch: Int,
  val gasUsed: GasUsed,
  val sharedObjects: List<SharedObject>? = null,
  val transactionDigest: String,
  val created: List<CreatedObject> ? = null,
  val mutated: List<MutateObject>,
  val gasObject: GasObject,
  val events: List<Event>,
  val dependencies: List<String>
)
