package xyz.mcxross.ksui.util

import xyz.mcxross.bcs.Bcs

inline fun <reified T> bcs(data: T): ByteArray = Bcs.encodeToByteArray(data)
