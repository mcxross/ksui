package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable

@Serializable
sealed class TypeTag {
  abstract fun asMoveType(): String
}

object U8 : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "u8"
}

object U16 : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "u16"
}

object U32 : TypeTag() {
  override fun asMoveType(): String = toString()

  override fun toString(): String = "u32"
}

object U64 : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "u64"
}

object U128 : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "u128"
}

object U256 : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "u256"
}

object Bool : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "bool"
}

object Address : TypeTag() {
  override fun asMoveType(): String = toString()
  override fun toString(): String = "address"
}

class Vector : TypeTag() {

  private var of: TypeTag? = null
  fun of(typeTag: TypeTag): Vector {
    of = typeTag
    return this
  }
  override fun asMoveType(): String = toString()
  override fun toString(): String = "vector<$of>"
}

fun vectorOf(typeTag: TypeTag): Vector {
  return Vector().of(typeTag)
}
