package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class DryRunTransactionResponse(val effects: TransactionEffects, val events: List<Event>)
