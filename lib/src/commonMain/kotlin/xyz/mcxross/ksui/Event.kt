package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable data class EventID(val txDigest: String, val eventSeq: Long)

@Serializable
data class EventEnvelope(
    val timestamp: Long,
    val txDigest: String,
    val id: EventID,
    val event: Event,
)

@Serializable data class Event(val transferObject: TransferObject)
