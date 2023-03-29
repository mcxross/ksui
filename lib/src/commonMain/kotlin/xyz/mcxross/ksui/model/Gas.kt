package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Gas(val objectId: String, val version: Int, val digest: String)

@Serializable
data class GasUsed(val computationCost: Int, val storageCost: Int, val storageRebate: Int)

@Serializable data class GasObject(
    val owner: AddressOwner,
    val reference: ObjectReference
)

@Serializable
data class GasData(val payment: Payment, val owner: String, val price: Int, val budget: Int)

@Serializable
data class GasCostSummary(
  val computationCost: Long,
  val storageCost: Long,
  val storageRebate: Long,
)

@Serializable data class GasPrice(@SerialName("result") val cost: Long)

