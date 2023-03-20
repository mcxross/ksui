package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable data class TransferObjectRequestParams(val recipient: String, val objectId: String)
