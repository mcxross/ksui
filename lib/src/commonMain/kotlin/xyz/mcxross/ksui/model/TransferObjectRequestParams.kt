package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class TransferObjectRequestParams(val recipient: String, val objectId: String)
