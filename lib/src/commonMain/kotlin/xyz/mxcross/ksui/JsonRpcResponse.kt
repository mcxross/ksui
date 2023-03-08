package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable data class JsonRpcResponse(val json: String, val result: Result)
