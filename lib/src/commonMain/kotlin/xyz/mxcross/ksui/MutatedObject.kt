package xyz.mxcross.ksui

import kotlinx.serialization.Serializable

@Serializable data class MutatedObject(val owner: AddressOwner, val reference: ObjectReference)
