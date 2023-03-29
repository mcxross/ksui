package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class ImmOrOwnedMoveObject(val objectId: String, val version: Int, val digest: String)
