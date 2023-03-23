package xyz.mcxross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class MutateObject {
  abstract val reference: ObjectReference
  @Serializable
  data class Shared(
    @SerialName("initial_shared_version") val initialSharedVersion: Int,
  )
  @Serializable
  data class Owner(
    @SerialName("Shared") val shared: Shared? = null,
    @SerialName("AddressOwner") val addressOwner: String? = null
  )
  @Serializable
  data class MutatedObjectShared(val owner: Owner, override val reference: ObjectReference) :
    MutateObject()
  @Serializable
  data class MutatedObject(val owner: Owner, override val reference: ObjectReference) :
    MutateObject()
}
