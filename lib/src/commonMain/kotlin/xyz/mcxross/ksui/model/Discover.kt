package xyz.mcxross.ksui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
  val name: String,
  val url: String,
  val email: String,
)

@Serializable
data class License(
  val name: String,
  val url: String,
)

@Serializable
data class Info(
  val title: String,
  val description: String,
  val contact: Contact,
  val license: License,
  val version: String,
)

@Serializable
data class Tag(
  val name: String,
)

@Serializable
data class Schema(
  val type: String? = null,
  val format: String? = null,
  val minimum: Double? = null,
  @SerialName("\$ref") val ref: String? = null,
)

@Serializable
data class Param(
  val name: String,
  val description: String? = null,
  val required: Boolean? = null,
  val schema: Schema,
)

@Serializable
data class Result(
  val name: String,
  val required: Boolean,
  val schema: Schema,
)

@Serializable
data class Method(
  val name: String,
  val tags: List<Tag>,
  val description: String,
  val params: List<Param>,
  val result: Result,
)

@Serializable
data class AuthorityPublicKeyBytes(
  val description: String,
  val allOf: List<Schema>,
)

@Serializable
data class CoinObjectCount(
  val type: String,
  val format: String,
  val minimum: Int,
)

@Serializable
data class CoinType(
  val type: String,
)

@Serializable
data class AdditionalProperties(
  val type: String,
  val format: String,
  val minimum: Int,
)

@Serializable
data class LockedBalanceProperties(
  val type: String,
  val additionalProperties: AdditionalProperties,
)

@Serializable
data class TotalBalance(
  val type: String,
  val format: String,
  val minimum: Int,
)

@Serializable
data class Properties(
  val coinObjectCount: CoinObjectCount,
  val coinType: CoinType,
  val lockedBalance: LockedBalanceProperties,
  val totalBalance: TotalBalance,
)

@Serializable
data class DiscoverBalance(
  val type: String,
  val required: List<String>,
  val properties: Properties,
)

@Serializable
data class DiscoverBalanceChange(
  val type: String,
  val required: List<String>,
)

@Serializable
data class SchemaItem(
  @SerialName("AuthorityPublicKeyBytes") val authorityPublicKeyBytes: AuthorityPublicKeyBytes,
  @SerialName("Balance") val balance: DiscoverBalance,
  @SerialName("BalanceChange") val balanceChange: DiscoverBalanceChange,
)

@Serializable
data class Components(
  val schemas: SchemaItem,
)

@Serializable
data class Discover(
  val openrpc: String,
  val info: Info,
  val methods: List<Method>,
)
