package xyz.mcxross.ksui.util

import xyz.mcxross.ksui.model.TransactionDigest

/** Helper function to create a list of [TransactionDigest]s from a list of [String]s. */
fun listOfTnxDigests(vararg digests: String): List<TransactionDigest> {
  return digests.map { TransactionDigest(it) }
}
