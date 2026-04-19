package xyz.mcxross.ksui.core.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.core.serializer.OptionSerializer

@Serializable(with = OptionSerializer::class)
sealed class Option<out T> {
  @Serializable data class Some<T>(val value: T) : Option<T>()

  @Serializable object None : Option<Nothing>()

  fun expect(message: String): T =
    when (this) {
      is Some -> value
      is None -> throw NoSuchElementException(message)
    }
}
