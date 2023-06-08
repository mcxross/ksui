package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.model.serializer.OptionSerializer

@Serializable(with = OptionSerializer::class)
sealed class Option<out T> {
  @Serializable data class Some<T>(val value: T) : Option<T>()
  @Serializable object None : Option<Nothing>()
}
