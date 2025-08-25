package xyz.mcxross.ksui.data

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyCredentialRes(
    val id: String,
    val type: String,
    val rawId: String,
    val response: AuthenticatorResponse
)

@Serializable
data class AuthenticatorResponse(
    val clientDataJSON: String,
    val authenticatorData: String,
    val signature: String,
    val userHandle: String? = null
)
