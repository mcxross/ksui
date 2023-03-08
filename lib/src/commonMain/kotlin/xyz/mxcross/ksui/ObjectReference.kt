package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class ObjectReference(val objectId: String, val version: Int, val digest: String)
