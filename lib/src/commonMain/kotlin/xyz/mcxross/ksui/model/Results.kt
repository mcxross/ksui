package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class Results(val transaction: TransactionData, val effects: Effects, val events: List<Event>)
