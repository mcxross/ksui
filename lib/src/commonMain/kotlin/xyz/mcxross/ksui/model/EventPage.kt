package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class NextCursor(val txDigest: String, val eventSeq: Long)

data class EventPage(
  val data: List<EventEnvelope>,
  val nextCursor: NextCursor,
)
