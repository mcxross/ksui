package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Transaction(val hash: String)

@Serializable data class TransactionNumber(@SerialName("result") val value: Long)
