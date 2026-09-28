package xyz.mcxross.ksui.core.model

/** Spendable funds of one coin type, separated by storage system. Amounts use base units. */
data class Holdings(
  val coinType: String,
  val coinObjects: ULong,
  val addressBalance: ULong,
) {
  val total: ULong
    get() {
      val sum = coinObjects + addressBalance
      require(sum >= coinObjects) { "Holdings total exceeds u64" }
      return sum
    }
}

/** Selects which storage system supplies a client-backed PTB funding request. */
enum class FundingSource {
  CoinObjects,
  AddressBalance,
  Auto,
}
