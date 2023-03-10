package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ValueRaw(val value: Long)

@Serializable data class SupplyRaw(@SerialName("result") val valueRaw: ValueRaw)

@Serializable data class Supply(val value: Long)
