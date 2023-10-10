package xyz.mcxross.ksui.util

import xyz.mcxross.ksui.model.Argument
import xyz.mcxross.ksui.model.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.model.TransactionDigest

/** Extension function to create a [TransactionDigest] from a [String]. */
fun String.toTxnDigest(): TransactionDigest = TransactionDigest(this)

/** Extension function to create a [SuiAddress] from a [String]. */
fun String.toSuiAddress(): SuiAddress = SuiAddress(this)

/** Extension functions to create [Argument.Input]s from various types. */
inline fun <reified T : Any> ProgrammableTransactionBuilder.inputs(
  vararg inputs: T
): List<Argument.Input> {
  return inputs.map { input(it) }
}
