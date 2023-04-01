package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Owner {
  @Serializable data class AddressOwner(@SerialName("AddressOwner") val address: String) : Owner

  @Serializable data class ObjectOwner(@SerialName("ObjectOwner") val address: String) : Owner

  @Serializable class SharedOwner(@SerialName("Shared") val shared: Shared) : Owner {
    @Serializable
    data class Shared(@SerialName("initial_shared_version") val initialSharedVersion: Long)
  }
}
