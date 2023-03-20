package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class ImmOrOwnedMoveObject(val objectId: String, val version: Int, val digest: String)
