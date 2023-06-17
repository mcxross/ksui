package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.ObjectResponseSerializer
import xyz.mcxross.ksui.model.serializer.OwnerSerializer

@Serializable data class ObjectId(val hash: String)

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

@Serializable
data class TransferredGasObject(val amount: Int, val id: String, val transferTxDigest: String)

@Serializable
sealed class FilterCondition {
  @Serializable data class MatchAll(val of: List<FilterCondition>)
  @Serializable data class MatchAny(val of: List<FilterCondition>)
  @Serializable data class MatchNone(val of: List<FilterCondition>)
  @Serializable data class StructType(val value: String) : FilterCondition()
  @Serializable data class AddressOwner(val value: String) : FilterCondition()
  @Serializable data class Version(val value: String) : FilterCondition()
}

@Serializable data class Filter(val condition: FilterCondition)

@Serializable
data class ObjectResponseQuery(val filter: Filter, val options: ObjectResponse.ObjectDataOptions)

@Serializable
data class ObjectInfo(
    val objectId: String,
    val version: String,
    val digest: String,
    val type: String,
    /*val owner: Owner,*/
    val previousTransaction: String,
    val storageRebateL: String
)

@Serializable data class ObjectData(val data: ObjectInfo)

@Serializable
data class ObjectsPage(
    val data: List<ObjectData>,
    val nextCursor: String? = null,
    val hasNextPage: Boolean
)

@Serializable data class LoadedChildObject(val objectId: String, val sequenceNumber: String)

@Serializable
data class LoadedChildObjectsResponse(val loadedChildObjects: List<LoadedChildObject>)

@Serializable data class PastObjectResponse(val status: String, val details: ObjectResponse)

@Serializable data class PastObjectRequest(val objectId: ObjectId, val version: Long)

@Serializable data class DynamicFieldName(val type: String, val value: String)

@Serializable
data class ImmOrOwnedMoveObject(val objectId: String, val version: Int, val digest: String)
