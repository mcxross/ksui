package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.ObjectResponseSerializer
import xyz.mcxross.ksui.model.serializer.OwnerSerializer
import xyz.mcxross.ksui.model.serializer.ResponseSerializer

@Serializable class ObjectID

@Serializable
data class Object(
  @Serializable(with = OwnerSerializer::class) val owner: Owner,
  val reference: ObjectReference,
)

@Serializable
data class SharedObject(
  val objectId: String,
  val version: Long,
  val digest: String,
)

data class SuiObjectInfo(
  var objectId: String,
  val version: Long,
  val digest: Digest,
  val type: String,
  val owner: Owner.AddressOwner,
  val previousTransaction: Transaction,
)

@Serializable
abstract class ObjectChange {
  abstract val type: String
  abstract val sender: String
  @Serializable(with = OwnerSerializer::class) abstract val owner: Owner
  abstract val objectType: String
  abstract val objectId: String
  abstract val version: Long
  abstract val previousVersion: Long
  abstract val digest: String

  @Serializable
  data class DefaultObject(
    override val type: String = "DefaultObject",
    override val sender: String = "DefaultObject",
    @Serializable(with = OwnerSerializer::class)
    override val owner: Owner = Owner.AddressOwner("DefaultObject"),
    override val objectType: String = "DefaultObject",
    override val objectId: String = "DefaultObject",
    override val version: Long = 0,
    override val previousVersion: Long = 0,
    override val digest: String = "DefaultObject",
  ) : ObjectChange()

  @Serializable
  data class MutatedObject(
    override val type: String,
    override val sender: String,
    @Serializable(with = OwnerSerializer::class) override val owner: Owner,
    override val objectType: String,
    override val objectId: String,
    override val version: Long,
    override val previousVersion: Long,
    override val digest: String,
  ) : ObjectChange()

  @Serializable
  data class CreatedObject(
    override val type: String,
    override val sender: String,
    @Serializable(with = OwnerSerializer::class) override val owner: Owner,
    override val objectType: String,
    override val objectId: String,
    override val version: Long,
    override val previousVersion: Long,
    override val digest: String,
  ) : ObjectChange()
}
@Serializable(with = ObjectResponseSerializer::class)
sealed class ObjectResponse {
  @Serializable
  data class ObjectDataOptions(
    val showType: Boolean,
    val showOwner: Boolean,
    val showPreviousTransaction: Boolean,
    val showDisplay: Boolean,
    val showContent: Boolean,
    val showBcs: Boolean,
    val showStorageRebate: Boolean,
  )

  @Serializable
  data class ObjectDataContent(
    val dataType: String,
    val type: String,
    val hasPublicTransfer: Boolean,
  )

  @Serializable
  data class ObjectDataBcs(
    val dataType: String,
    val type: String,
    val hasPublicTransfer: Boolean,
    val version: Long,
    val bcsBytes: String = "",
  )

  @Serializable
  data class ObjectData(
    val objectId: String,
    val version: Long,
    val digest: String,
    val type: String = "",
    val owner: Owner.AddressOwner? = null,
    val previousTransaction: String = "",
    val storageRebate: Long = 0,
    val content: ObjectDataContent? = null,
  ) : ObjectResponse()

  @Serializable
  data class ObjectResponseError(
    val code: String,
    @SerialName("object_id") val objectId: String,
  ) : ObjectResponse()

}
