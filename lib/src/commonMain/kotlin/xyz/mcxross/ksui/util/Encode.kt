package xyz.mcxross.ksui.util

import xyz.mcxross.bcs.Bcs

inline fun <reified T> bcsEncode(data: T): ByteArray = Bcs.encodeToByteArray(data)

inline fun <reified T> bcsDecode(data: ByteArray): T = Bcs.decodeFromByteArray(data)
