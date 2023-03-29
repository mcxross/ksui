package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ObjectOwner(@SerialName("ObjectOwner") val address: SuiAddress)
