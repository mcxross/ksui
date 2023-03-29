package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatedObject(
  val owner: AddressOwner,
  val reference: ObjectReference,
)
