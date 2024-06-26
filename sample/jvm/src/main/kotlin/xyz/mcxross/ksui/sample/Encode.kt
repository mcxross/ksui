package xyz.mcxross.ksui.sample

import org.bouncycastle.util.encoders.Hex

fun bytesToHex(bytes: ByteArray): String = Hex.toHexString(bytes)

