package xyz.mcxross.ksui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class EventID(val txDigest: String, val eventSeq: Long)

@Serializable
data class EventEnvelope(
  val timestamp: Long,
  val txDigest: String,
  val id: EventID,
  val event: Event,
)

@Serializable
abstract class Event {
  abstract val packageId: String
  abstract val transactionModule: String
  abstract val sender: String
  @Serializable
  data class CoinBalanceChangeEvent(
    override val packageId: String,
    override val transactionModule: String,
    override val sender: String,
    val changeType: String,
    val owner: AddressOwner,
    val coinType: String,
    val coinObjectId: String,
    val version: Int,
    val amount: Long,
  ) : Event()

  @Serializable
  data class MutateObjectEvent(
    override val packageId: String,
    override val transactionModule: String,
    override val sender: String,
    val objectType: String,
    val objectId: String,
    val version: Int,
  ) : Event()

  @Serializable
  data class ObjectCreateEvent(
    override val packageId: String,
    override val transactionModule: String,
    override val sender: String,
    val recipient: AddressOwner,
    val objectType: String,
    val objectId: String,
    val version: Int,
  ) : Event()

  @Serializable
  data class AttributeFields(
    val name: String,
    val value: String,
  )
  @Serializable
  data class Attribute(
    val type: String,
    val fields: AttributeFields,
  )

  @Serializable data class DevGeneFields(val sequence: List<Int>)

  @Serializable
  data class DevGene(
    val type: String,
    val fields: DevGeneFields,
  )
  @Serializable
  data class Field(
    val attributes: List<Attribute>,
    @SerialName("bred_by") val bredBy: String,
    @SerialName("dev_genes") val devGenes: DevGene,
    val gen: Int,
    val genes: MoveGene,
    val id: String,
    @SerialName("parent_one") val parentOne: String,
    @SerialName("parent_two") val parentTwo: String,
  )

  @Serializable data class MoveGeneFields(val sequence: List<Int>)
  @Serializable
  data class MoveGene(
    val type: String,
    val fields: MoveGeneFields,
  )
  @Serializable
  @SerialName("moveEvent")
  data class MoveEvent(
    override val packageId: String,
    override val transactionModule: String,
    override val sender: String,
    val type: String?,
    val fields: Field?,
    val bcs: String?,
  ) : Event()

  @Serializable
  data class EventObject(
    val moveEvent: MoveEvent?,
    val newObject: ObjectCreateEvent?,
    val mutateObject: MutateObjectEvent?,
    val coinBalanceChange: CoinBalanceChangeEvent?,
  )
}
