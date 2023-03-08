package xyz.mxcross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ObjectID(@SerialName("ObjectID") val value: String)
