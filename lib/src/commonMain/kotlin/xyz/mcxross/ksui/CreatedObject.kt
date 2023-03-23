package xyz.mcxross.ksui

import kotlinx.serialization.Serializable

@Serializable
data class CreatedObject(
  val owner: AddressOwner,
  val reference: ObjectReference,
)
