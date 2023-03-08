package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class TransactionQuery(@SerialName("InputObject") val inputObject: InputObject)
