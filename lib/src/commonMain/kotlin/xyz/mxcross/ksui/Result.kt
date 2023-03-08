package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class Result(val transaction: TransactionData, val effects: Effects, val events: List<Event>)
