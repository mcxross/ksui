package xyz.mcxross.ksui.core.model

import kotlinx.serialization.Serializable

@Serializable data class RawQuery(val query: String, val variables: Map<String, String>? = null)
