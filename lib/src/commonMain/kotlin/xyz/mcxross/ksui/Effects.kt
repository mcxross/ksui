package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class Effects(
  val status: Status,
  val executedEpoch: Int,
  val gasUsed: GasUsed,
  val sharedObjects: List<SharedObject>,
  val transactionDigest: String,
  val created: List<CreatedObject>,
  val mutated: List<MutateObject>,
  val gasObject: GasObject,
  /*val events: List<Event.EventObject>,*/
  val dependencies: List<String>
)
