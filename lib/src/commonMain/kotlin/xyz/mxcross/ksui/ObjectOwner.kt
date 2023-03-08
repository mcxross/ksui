package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ObjectOwner(@SerialName("ObjectOwner") val address: SuiAddress)
