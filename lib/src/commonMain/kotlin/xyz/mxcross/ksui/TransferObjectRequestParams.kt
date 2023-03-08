package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable data class TransferObjectRequestParams(val recipient: String, val objectId: String)
