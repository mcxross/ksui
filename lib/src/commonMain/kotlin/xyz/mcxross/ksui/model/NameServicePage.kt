package xyz.mcxross.ksui.model

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class NameServicePage(
  @Required val data: List<String>,
  @Required val hasNextPage: Boolean,
  val nextCursor: String? = null,
)
