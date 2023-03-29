package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable data class SuiPastObjectResponse(val status: String, val details: Details)
