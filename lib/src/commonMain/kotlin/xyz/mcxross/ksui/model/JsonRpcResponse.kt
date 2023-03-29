package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class JsonRpcResponse(val json: String, val results: Results)
