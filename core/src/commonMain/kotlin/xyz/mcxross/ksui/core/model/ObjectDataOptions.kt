package xyz.mcxross.ksui.core.model

/**
 * Object data options
 *
 * This class represents the options for object data
 *
 * @property showBcs Show the BCS
 * @property showContent Show the content
 * @property showDisplay Show the display
 * @property showType Show the type
 * @property showOwner Show the owner
 * @property showPreviousTransaction Show the previous transaction
 * @property showStorageRebate Show the storage rebate
 */
data class ObjectDataOptions(
  val showBcs: Boolean = false,
  val showContent: Boolean = false,
  val showDisplay: Boolean = false,
  val showType: Boolean = false,
  val showOwner: Boolean = false,
  val showPreviousTransaction: Boolean = false,
  val showStorageRebate: Boolean = false,
)
