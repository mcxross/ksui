package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class TransactionQuery(@SerialName("InputObject") val inputObject: InputObject)
