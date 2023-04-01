package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class Payment(val objectId: String, val version: Long, val digest: String)
