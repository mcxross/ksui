package xyz.mcxross.ksui.extension

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.SuiAddress

fun String.toReference() = Reference(AccountAddress(this))

fun String.toObjectDigest() = ObjectDigest(Digest(this))

/** Extension function to create a [SuiAddress] from a [String]. */
fun String.toSuiAddress() = SuiAddress(this)
